package io.github.agentsoz.matsimmelbourne.activetransport;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.contrib.bicycle.BicycleConfigGroup;
import org.matsim.contrib.bicycle.Bicycles;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.network.algorithms.MultimodalNetworkCleaner;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RunBicycleScenario {
    private static final Logger LOG = Logger.getLogger(RunBicycleScenario.class);

	private String configFile;
	private String inputDir;

    private final Config config;
    private final Scenario scenario;
    private final Controler controler;
    private Network network;
    private String cleanNetwork;

    public static void main(String[] args) throws FileNotFoundException, IOException {
        RunBicycleScenario rbs = new RunBicycleScenario(args[0], args[1], args[2], args[3]);
        rbs.run();
    }

    public RunBicycleScenario(String inputDir, String outputDir, String configName, String cleanNetwork) throws FileNotFoundException, IOException {
	
        this.configFile = inputDir + "/" + configName; // e.g. "./scenarios/active-transport-IVABM/config_IVABMPlan.xml"
        this.inputDir = inputDir;
        this.cleanNetwork=cleanNetwork; //true or false
        this.config = ConfigUtils.loadConfig(configFile, new BicycleConfigGroup());
        config.controler().setOutputDirectory(outputDir);
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        //config.plansCalcRoute().setInsertingAccessEgressWalk(true);
        //config.plansCalcRoute().clearModeRoutingParams(true);
        fillConfigWithBicycleStandardValues(config);

        this.scenario = ScenarioUtils.loadScenario(config);
        createVehiclesForScenario(scenario);

        if (cleanNetwork.equals("true")) {
            System.out.println("About to clean the network");
            this.network = this.scenario.getNetwork();
            cleanNetworkForCars(network);
        }

        this.controler = new Controler(scenario);
    }

    private static void cleanNetworkForCars(Network network){
        System.out.println("cleaning the network");

        Set<String> mode_Set = new HashSet<String>();
        mode_Set.add("car");
        //mode_Set.add("bicycle");
        //new MultimodalNetworkCleaner(network).run(Collections.singleton(TransportMode.car));
        new MultimodalNetworkCleaner(network).run(mode_Set);
        //new NetworkCleaner().run(network);
        new NetworkWriter(network).write("./cleanedNetwork_car.xml.gz");

        Set<String> mode_Set2 = new HashSet<String>();
        mode_Set2.add("bicycle");
        //new MultimodalNetworkCleaner(network).run(Collections.singleton(TransportMode.car));
        new MultimodalNetworkCleaner(network).run(mode_Set2);
        //new NetworkCleaner().run(network);
        new NetworkWriter(network).write("./cleanedNetwork_carBicycle.xml.gz");

    }
    private static void fillConfigWithBicycleStandardValues(Config config){

        config.controler().setWriteEventsInterval(1);

        BicycleConfigGroup bicycleConfigGroup = (BicycleConfigGroup) config.getModules().get(BicycleConfigGroup.GROUP_NAME);
        bicycleConfigGroup.setMarginalUtilityOfInfrastructure_m(-0.02);
        bicycleConfigGroup.setMarginalUtilityOfComfort_m(-0.0002);
        bicycleConfigGroup.setMarginalUtilityOfGradient_m_100m(-0.02);
        bicycleConfigGroup.setMaxBicycleSpeedForRouting(4.16666666);

        List<String> mainModeList = new ArrayList<>();
        mainModeList.add("bicycle");
        mainModeList.add(TransportMode.car);
        config.qsim().setMainModes(mainModeList);
        config.plansCalcRoute().setNetworkModes(mainModeList);
    }

    private static void createVehiclesForScenario(Scenario scenario){

        VehicleType car = VehicleUtils.getFactory().createVehicleType(Id.create(TransportMode.car, VehicleType.class));
        scenario.getVehicles().addVehicleType(car);

        VehicleType bicycle = VehicleUtils.getFactory().createVehicleType(Id.create("bicycle", VehicleType.class));
        bicycle.setMaximumVelocity(20.0 / 3.6);
        bicycle.setPcuEquivalents(0.25);
        scenario.getVehicles().addVehicleType(bicycle);
        scenario.getConfig().qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);
    }

    public void run(){
        Bicycles.addAsOverridingModule(controler);
        controler.run();
    }
}
