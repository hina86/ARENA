requiredPackages = c('ggpubr', 'ggplot2', 'RColorBrewer')
for(p in requiredPackages){
  if(!require(p,character.only = TRUE)) install.packages(p, repos = "http://cran.us.r-project.org", source = "binary", dependencies = TRUE)
}

args <- commandArgs(TRUE)
xProp <- args[1]
yProp <- args[2]
facets <- eval(parse(text=args[3]))
orderArr <- eval(parse(text=args[4]))
colorArr <- eval(parse(text=args[5]))
xLabel <- args[6]
yLabel <- args[7]
dataPath <- args[8]
resultPath <- args[9]
cap <- args[10]
w <- args[11]
h <- args[12]
unt <- args[13]
removeLegend <- args[14]
filters1 <- eval(parse(text=args[15]))
filters2 <- eval(parse(text=args[16]))
filterVar1 <- args[17]
filterVar2 <- args[18]

library("ggpubr")
library("RColorBrewer")
my_data <- read.csv(dataPath)
if(!is.null(filterVar1)){
  if(filterVar1 != "none" && !is.null(filters1)){
    print("filtering 1")
    my_data <- subset(my_data, my_data[[filterVar1]] %in% filters1) # filtering by property value
    
  }
}
if(!is.null(filterVar2)){
  if(filterVar2 != "none" && !is.null(filters2)){
    my_data <- subset(my_data, my_data[[filterVar2]] %in% filters2) # filtering by property value
    
  }
}
min <- min(as.numeric(my_data[[yProp]]))
max <- max(as.numeric(my_data[[yProp]]))
min
max
png(resultPath, width = as.numeric(w), height = as.numeric(h), units = unt, res = 300)
finalGraph <- ggscatter(my_data, x = xProp, y = yProp, 
                        order = orderArr,
                        palette = colorArr,
                        ylim = c(min, max),
                        color = xProp,
                        ylab = yLabel, xlab = xLabel, conf.int = TRUE, 
                        point = FALSE) +
  geom_point(aes( shape = my_data[[xProp]], color = my_data[[xProp]]), alpha = 0.6, stroke = 0.5) +
  guides(col=guide_legend(xLabel), shape=guide_legend(xLabel), color = guide_legend(nrow = 2, byrow = TRUE))+
  scale_size(range = c(0.3, 6)) + 
  labs(caption = cap) + 
  grids(axis = c("y"), linetype = "solid") + 
  theme(legend.title = element_text(size = (6), face = "bold.italic", family = "Helvetica"), 
        legend.text = element_text(size = (6), face = "italic", family = "Helvetica"),
        legend.margin = margin(0.5,0.5,0.5,0.5),
        legend.key.width = unit(0.2,"cm") ,
        legend.key.height = unit(0.2,"cm") ,
        legend.position = "top",
        axis.text.x = element_text(size = (7), angle = 90, hjust=1),
        axis.text.y = element_text(size = (7)),
        axis.title.x = element_text(family = "Helvetica", size = (8), colour = "black"),
        axis.title.y = element_text(family = "Helvetica", size = (8), colour = "black"),
        plot.caption = element_text(family = "Helvetica", colour = "black", hjust = .5, size = 9, face = "italic"), 
        panel.border = element_rect(linetype = "solid", fill = NA))   
if(length(colorArr)==0){
  finalGraph <- finalGraph +   scale_color_brewer(palette = "Blues")
} 
if(removeLegend){
  finalGraph <- finalGraph + theme(legend.title=element_blank(), 
                                   legend.text = element_blank(),
                                   legend.position = "none")
}
if(!is.null(facets)){
  finalGraph <- facet(finalGraph, facet.by = facets,  strip.position = "top")
}

print(finalGraph)
invisible(dev.off())