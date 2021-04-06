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

if(!file.exists(paste(resultPath, "/Pairwise.docx", sep=""))){
  doc = read_docx()
  body_add_fpar(doc, fpar(ftext("Paired Sample t-test", prop=fp_text( bold = TRUE, font.size=14))))
  body_add_par(doc, "", style = "Normal")
  body_add_fpar(doc, fpar(ftext("Introduction", prop=fp_text( bold = TRUE, font.size=12))))
  body_add_par(doc, "Pairwise comparisons using Wilcoxon rank sum test with continuity correction ", style = "Normal")
  body_add_par(doc, "", style = "Normal")
  body_add_par(doc, "Statistical significance is determined by looking at the p-value. The p-value gives the probability of observing the test results under the null hypothesis. The lower the p-value, the lower the probability of obtaining a result like the one that was observed if the null hypothesis was true. Thus, a low p-value indicates decreased support for the null hypothesis. However, the possibility that the null hypothesis is true and that we simply obtained a very rare result can never be ruled out completely. The cutoff value for determining statistical significance is ultimately decided on by the researcher, but usually a value of .05 or less is chosen. This corresponds to a 5% (or less) chance of obtaining a result like the one that was observed if the null hypothesis was true.", 
               style = "Normal")
  body_add_par(doc, "", style = "Normal")
  body_add_fpar(doc, fpar(ftext("Results", prop=fp_text( bold = TRUE, font.size=12))))
} else {
  doc = read_docx(paste(resultPath, "/Pairwise.docx", sep=""))
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
    pp <-pairwise.wilcox.test(my_data[[depVar]], my_data[[indVar]],
                              p.adjust.method = "BH")
    pp
    pvals <- pp$p.value
    text <- as.table(pvals)
    df <- data.frame(text)
    print(df)
    vec <- numeric()
    for(row in 1:nrow(df)){
      print(df[row, 3])
      if(toString(df[row, 3]) == "NA"){
        vec <- c(vec, row)
      }
    }
    df <- df[-vec, ]
    df$Var1 <- paste(df$Var1, df$Var2, sep="-") 
    df <- df[, -c(2) ]
    df <- setNames(df, c(indVar,"p-value"))
    body_add_par(doc, "", style="Normal")
    body_add_par(doc, "", style = "Normal")
    
    body_add_fpar(doc, fpar(ftext(paste("Pairwise comparisons using Wilcoxon rank sum exact test for",
                                        indVar, "and", depVar), prop=fp_text( italic = TRUE, font.size=11))))
    body_add_par(doc, "", style = "Normal")
    body_add_table(doc, df, style="Table Professional")
    body_add_par(doc, "", style="Normal")
  }
}
print(doc, target=paste(resultPath, "/Pairwise Test.docx", sep=""))
