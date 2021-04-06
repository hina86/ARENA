requiredPackages = c('officer', 'dplyr', 'ggpubr')
for(p in requiredPackages){
  if(!require(p,character.only = TRUE)) install.packages(p, repos = "http://cran.us.r-project.org", dependencies = TRUE)
}

args <- commandArgs(TRUE)
dataPath <- args[1]
depVars <- eval(parse(text=args[2]))
indVars <- eval(parse(text=args[3]))
filterVar <- args[4]
filterVal <- args[5]
resultPath <- args[6]


library(officer)
library(dplyr)
specify_decimal <- function(x, k) trimws(format(round(x, k), nsmall=k))

if(!file.exists(paste(resultPath, "/Shapiro Wilk.docx", sep=""))){
  doc = read_docx()
  body_add_fpar(doc, fpar(ftext("Shapiro Wilk Test", prop=fp_text( bold = TRUE, font.size=14))))
  body_add_par(doc, "")
  body_add_par(doc, "The assumption of normality will be examined with a one-sample Shapiro-Wilk test (Razali & Wah, 2011). ", style = "Normal")
  body_add_par(doc, "")
  body_add_fpar(doc, fpar(ftext("Results", prop=fp_text( bold = TRUE, font.size=12))))} else {
    doc = read_docx(paste(resultPath, "/Shapiro Wilk.docx", sep=""))
  }
body_add_par(doc, "", style = "Normal")

my_data <- read.csv(dataPath)
if(filterVal != "none"){
  body_add_fpar(doc, fpar(ftext( paste("For", filterVar, "=", filterVal), prop=fp_text( bold = TRUE, font.size=12))))
  body_add_par(doc, "", style = "Normal")
  my_data <- subset(my_data, my_data[[filterVar]]==filterVal) # filtering by property value
}
for(indVar in indVars){
  for (depVar in depVars) {
    #Shapiro-Wilk test
    r <- aov(my_data[[depVar]] ~ my_data[[indVar]], data = my_data)
    aov_residuals <- residuals(object = r )   # Extract the residuals
    shp <- shapiro.test(x = aov_residuals )
    shp
    pval <- shp$p.value
    text <- ""
    sign <- ">"
    if(as.numeric(pval) > 0.05){
      text <- "data is normally distributed."
    } else {
      text <- "data significantly deviates from a normal distribution."
      sign <- "<"
    }
    finalTxt <- paste("The Shapiro-Wilk test for", depVar, "and", indVar, "shows that p=",pval, sign, "0.05",
                      "which means that", text)
    body_add_par(doc, finalTxt, style="Normal")
    body_add_par(doc, "", style="Normal")
    library(ggpubr)
    plot <- ggqqplot(my_data[[depVar]])
    body_add_gg(doc, plot, style="centered")
  }
}
print(doc, target=paste(resultPath, "/Shapiro Wilk.docx", sep=""))

