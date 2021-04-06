args <- commandArgs(TRUE)
dataPath <- args[1]
depVar <- args[2]
indVar <- args[3]
filterVar <- args[4]
filterVal <- args[5]
resultPath <- args[6]

library(officer)
library(dplyr)
library(e1071) 
my_data <- read.csv(dataPath)
if(filterVal != "none"){
  my_data <- subset(my_data, my_data[[filterVar]]==filterVal) # filtering by property value
}
#Kruskal Wallis test
kw <-kruskal.test(my_data[[depVar]] ~ my_data[[indVar]], data = my_data)
kw
dd <- data.frame(unlist(kw))
chisq <- dd[1, 1]
df <- dd[2, 1]
p <- dd[3, 1]
testMethod <- dd[4,1]
testBtw <- dd[5, 1]
sign <- "<"
if(as.numeric(p)> 0.001){
  sign <= ">"
}
doc=read_docx()
body_add_par(doc,"Summary Statistics: ", style="heading 2")
text <- paste("The results of the Kruskal-Wallis test were significant based on an alpha value of 0.05, ",
              "chi-squared(", df, ") =",chisq,", p" , sign ,"0.001", 
               "indicating that the mean rank of energy was significantly different between the levels of library.")
body_add_par(doc, text)
body_add_par(doc, "")#empty line
body_add_par(doc, paste(testMethod, "for", testBtw))

dff <- group_by(my_data, my_data[[ indVar ]]) %>%
  summarise(
    Avg = mean(!!rlang::sym(depVar), na.rm = TRUE),
  ) %>%
  rename_at(paste("my_data[[",indVar,"]]") ~ indVar) 
dff


body_add(doc, dff)#table
require(graphics)
body_add_plot(doc, plot_instr(
  code = {boxplot(my_data[[depVar]] ~ my_data[[indVar]], data = my_data)}),
  style = "Normal" )#graph
# post-hoc test for identifying statistical significant differences between the groups
library(pgirmess)
posthoc <- kruskalmc(my_data[[depVar]] ~ my_data[[indVar]], data = my_data)
ph <- as.data.frame(posthoc)
nph <- data.frame("library" = rownames(ph), ph)
nph <- subset(nph, select = c(0,2,3))
print(nph)
body_add(doc, nph)
print(doc, target=paste(resultPath, "/summary.docx", sep=""))


