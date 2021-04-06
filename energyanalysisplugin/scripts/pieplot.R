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
print(filters1)
print(filters2)

library("ggpubr")
library("RColorBrewer")
my_data <- read.csv(dataPath)
if(filterVar1 != "none"){
  my_data <- subset(my_data, my_data[[filterVar1]] %in% filters1) # filtering by property value
}
if(filterVar2 != "none"){
  my_data <- subset(my_data, my_data[[filterVar2]] %in% filters2) # filtering by property value
}
colName <- noquote(xProp)
if(!is.null(orderArr)){
  my_data[[xProp]] <- ordered(my_data[[xProp]], levels = orderArr)
}
png(resultPath, width = as.numeric(w), height = as.numeric(h), units = unt, res = 300)
finalGraph <- ggplot(my_data, aes(x="",
                                  y=my_data[[yProp]],
                                  fill=my_data[[xProp]], 
                                  palette = colorArr), 
                     xL) +
  geom_bar(stat="identity", width=1, color="white") +
  guides(col=guide_legend(xLabel), size=guide_legend(yLabel))+
  coord_polar(theta="y") +
  grids(linetype = "solid") + 
  guides(fill=guide_legend(xLabel)) +
  labs(title = "", x="", y = yLabel) +
  labs(caption = cap) +
  geom_text(aes(label=sprintf("%0.2f", round(my_data[[yProp]], digits = 2))),
            color = "black", size=2, position = position_stack(vjust = 0.5))+
  theme(axis.text = element_blank(), 
        axis.ticks = element_blank(), 
        axis.line = element_blank(), 
        panel.border = element_rect(linetype = "solid", fill = NA), 
        legend.title = element_text(size = (6), face = "bold.italic", family = "Helvetica"), 
        legend.text = element_text(size = (6), face = "italic", family = "Helvetica"),
        legend.margin = margin(0.5,0.5,0.5,0.5),
        legend.key.width = unit(0.2,"cm") ,
        legend.key.height = unit(0.2, "cm") ,
        legend.position = "top",
        axis.title.x = element_text(family = "Helvetica", size = (8), colour = "black"),
        axis.title.y = element_text(family = "Helvetica", size = (8), colour = "black"),
        plot.caption = element_text(family = "Helvetica", colour = "black", hjust = .5, size = 9, face = "italic")
        ) 
if(length(colorArr)==0){
  finalGraph <- finalGraph +   scale_fill_brewer(palette = "Blues")
} 
if(removeLegend){
  finalGraph <- finalGraph + theme(legend.title=element_blank(), 
                                   legend.text = element_blank(),
                                   legend.position = "none")
}
if(!is.null(facets)){
  finalGraph <- facet(finalGraph, facet.by = facets,  strip.position = "top", 
                      panel.labs.font = list(size = 6)
  )
}
print(finalGraph)
invisible(dev.off())

