requiredPackages = c('officer', 'dplyr', 'ggpubr', 'RVAideMemoire', 'pgirmess')
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

if(!file.exists(paste(resultPath, "/Anova.docx", sep=""))){
  doc = read_docx()
  body_add_fpar(doc, fpar(ftext("Anova Test", prop=fp_text( bold = TRUE, font.size=14))))
  body_add_par(doc, "", style = "Normal")
  body_add_par(doc, "The assumptions of normality, homogeneity of variance, and outliers will be assessed. Normality assumes that residuals of the ANOVA follow a normal distribution (bell-shaped curve). Normality will be assessed graphically using a Q-Q scatterplot (Field, 2013; Bates, Mächler, Bolker, & Walker, 2014; DeCarlo, 1997). Homoscedasticity assumes that there is no underlying relationship between the residuals and the fitted values. The assumption will be examined with a scatterplot of the residuals and the fitted values (Field, 2013; Bates et al., 2014; Osborne & Walters, 2002). Outliers will be determined as any observation that has a studentized residual (Field, 2013; Stevens, 2009) that exceeds the .999 quantile of a t-distribution, with the degrees of freedom being n-1, where n is the sample size. The ANOVA will apply the F-test to determine if there are any significant differences at a significance level. If there are any significant effects, Tukey pairwise comparisons will be conducted to further examine the results.", style = "Normal") 
  body_add_par(doc, "", style = "Normal")
  body_add_fpar(doc, fpar(ftext("Results", prop=fp_text( bold = TRUE, font.size=12))))
} else {
  doc = read_docx(paste(resultPath, "/Anova.docx", sep=""))
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
    r <- aov(my_data[[depVar]] ~ library, data = my_data)
    df <- summary(r)
    df
    pval <- df[[1]][[1,"Pr(>F)"]]
    body_add_fpar(doc, fpar(ftext(paste("ANOVA test between", depVar, "and",indVar), prop=fp_text( bold = TRUE, font.size=12))))
    body_add_par(doc, paste("The p-value obtained from Anova test for", depVar, "of", indVar, "is ", pval), style="Normal")
    if(as.numeric(pval) > 0.05){
      body_add_par(doc, paste("As p-value > 0.05, hence the differences between means are not statistically significant."), style="Normal")
    } else {
      body_add_par(doc, paste("As p-value > 0.05, hence the differences between some of the means are statistically significant."), style="Normal")
    }
    body_add_par(doc, "", style = "Normal")
    
    body_add_fpar(doc, fpar(ftext("Tukey HSD", prop=fp_text( bold = TRUE, font.size=12))))
    
    aov_residuals <- residuals(object = r )   # Extract the residuals
    #Tukey HSD fitted on Anova 
    tuk <- TukeyHSD(r)
    str(tuk)
    print(df <- as.data.frame(tuk$library[,c(1,2,3,4)]))
    df <- data.frame(indVar = rownames(df), df)
    df <- setNames(df, c("Comparison", "Diff", "Lower", "Upper", "p adj"))
    body_add_par(doc, "", style = "Normal")
    body_add_fpar(doc, fpar(ftext(paste("Tukey HSD test for ",indVar , "and", depVar), prop=fp_text( italic = TRUE, font.size=11))))
    body_add_par(doc, "", style = "Normal")
    body_add_table(doc, df, style="Table Professional")
    body_add_par(doc, "", style="Normal")
  }
}
print(doc, target=paste(resultPath, "/Anova.docx", sep=""))

