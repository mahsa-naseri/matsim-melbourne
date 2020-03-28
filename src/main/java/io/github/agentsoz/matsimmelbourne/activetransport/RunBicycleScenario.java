package io.github.agentsoz.matsimmelbourne.activetransport;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.bicycle.BicycleConfigGroup;
import org.matsim.contrib.bicycle.Bicycles;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RunBicycleScenario {
    private static final Logger LOG = Logger.getLogger(RunBicycleScenario.class);

    private final Config config;
    private final Scenario scenario;
    private final Controler controler;

    public static void main(String[] args) throws FileNotFoundException, IOException {
        RunBicycleScenario rbs = new RunBicycleScenario();
        rbs.run();
    }

    public RunBicycleScenario() throws FileNotFoundException, IOException {
        this.config = ConfigUtils.loadConfig("./inputs/configs/config.xml", new BicycleConfigGroup());
        fillConfigWithBicycleStandardValues(config);
        this.scenario = ScenarioUtils.loadScenario(config);
        createVehiclesForScenario(scenario);
        this.controler = new Controler(scenario);
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
