cleanNetwork <- function(networkRestructured, network_modes = "car"){
  nodes_df <- networkRestructured[[1]]
  lines_df <- networkRestructured[[2]]
  get_biggest_component <- function(l_df,m){
    # Filtering links based on the mode
    l_df_mode <- l_df %>% filter(modes %like% m)
    # Making the graph for the intersections
    g <- graph_from_data_frame(dplyr::select(l_df_mode, from=from_id,to=to_id), directed = FALSE) 
    #plot(g,  vertex.size=0.1, vertex.label=NA, vertex.color="red", edge.arrow.size=0, edge.curved = 0)
    
    # Getting components
    comp <- components(g)
    
    nodes_in_largest_component <- data.frame(segment_id=as.character(names(comp$membership)), cluster_id=comp$membership, row.names=NULL) %>%
      filter(cluster_id==which.max(comp$csize)) %>%
      pull(segment_id) %>%
      base::unique()
    
    #n_df_filtered <- n_df %>%
    #  filter(id%in%nodes_in_largest_component)
    # AB 2020-08-14: I think this should be: l_df_filtered <- lines_df %>%
    # This would include walk paths provided they don't have any walk exclusive
    # nodes.
    l_df_filtered <- l_df_mode %>%
      filter(from_id%in%nodes_in_largest_component & to_id%in%nodes_in_largest_component)
    
    return(l_df_filtered)
  }
  
  lines_df  <- lines_df %>% 
    filter(from_id != to_id) %>% 
    filter(capacity != "NA" & capacity != "0.0" & modes != "NA")
  
  #if(ivabm_pt_flag){
  #  lines_df  <-lines_df %>% mutate(id = paste("p",from_id, to_id, row_number(), sep = "_"))%>%
  #    mutate(from_id=paste0("p_",from_id)) %>% 
  #    mutate(to_id=paste0("p_",to_id))  
  #  nodes_df <- nodes_df %>% mutate(id = paste0("p_",id))
  #}
    
  # Removing repetitive links
  lines_df <- lines_df %>% 
    distinct(from_id,to_id, .keep_all = TRUE)
  
  if(network_modes!=""){
    lines_df_filtered <- lines_df[0,]
    for (mode in network_modes){
      warning(paste('Cleaning network for ', mode))
      temp_df <- get_biggest_component(lines_df,mode)
      lines_df_filtered<- rbind(lines_df_filtered, temp_df)
    }
  }else if(network_modes==""){
    warning('Empty list of network modes, skip cleaning')
    lines_df_filtered <- lines_df
  }else{
    warning('Proper mode for cleaning is not provided, skip cleaning')
    lines_df_filtered <- lines_df
  }
  
  nodes_df_cleaned <- nodes_df %>% 
    filter(id %in% lines_df_filtered$from_id | id %in% lines_df_filtered$to_id)
  
  return(list(nodes_df_cleaned,lines_df_filtered))
}