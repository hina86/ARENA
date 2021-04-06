requiredPackages = c('officer', 'dplyr', 'ggpubr', 'propagate')
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
#functions
specify_decimal <- function(x, k) trimws(format(round(x, k), nsmall=k))
#loading data filtered 
my_data <- read.csv(dataPath)

#Filtering data by filter value
if(filterVar != "none" && filterVal != "none" && !is.null(filterVal)){
  my_data <- subset(my_data, my_data[[filterVar]]==filterVal) # filtering by property value
}
#Creating file of it does not exist and adding title headings
library(officer)
doc=NULL
if(!file.exists(paste(resultPath, "/Summary.docx", sep=""))){
  doc = read_docx()
  body_add_fpar(doc, fpar(ftext("Summary Statistics", prop=fp_text( bold = TRUE, font.size=14))))
  body_add_par(doc, "")
  body_add_fpar(doc, fpar(ftext("Description", prop=fp_text( bold = TRUE, font.size=12))))
  body_add_par(doc, "")
  body_add_fpar(doc, fpar(ftext("Mean (M): ", prop=fp_text( bold = TRUE, font.size=11)), 
                          ftext("The average value of a scale variable.", prop=fp_text(font.size=11)))
  )
  body_add_fpar(doc, fpar(ftext("Sample Size (n): ", prop=fp_text( bold = TRUE, font.size=11)), 
                          ftext("The frequency or count of a nominal or ordinal category.", prop=fp_text(font.size=11)))
  )
  body_add_fpar(doc, fpar(ftext("Sample Minimum (Min): ", prop=fp_text( bold = TRUE, font.size=11)), 
                          ftext("The smallest numeric value in a given sample.", prop=fp_text(font.size=11)))
  )
  body_add_fpar(doc, fpar(ftext("Sample Maximum (Max): ", prop=fp_text( bold = TRUE, font.size=11)), 
                          ftext("The largest numeric value in a given sample.", prop=fp_text(font.size=11)))
  )
  body_add_fpar(doc, fpar(ftext("Standard Deviation (SD): ", prop=fp_text( bold = TRUE, font.size=11)), 
                          ftext("The spread of the data around the mean of a scale variable.", prop=fp_text(font.size=11)))
  )
  body_add_fpar(doc, fpar(ftext("Standard Error of the Mean (SE", prop=fp_text( bold = TRUE, font.size=11)),
                          ftext("M", prop=fp_text( bold = TRUE, font.size=11, vertical.align = "subscript")),
                          ftext("): The estimate of how far the sample mean is likely to differ from the actual population mean.", prop=fp_text(font.size=11)))
  )
  body_add_fpar(doc, fpar(ftext("Skewness: ", prop=fp_text( bold = TRUE, font.size=11)), 
                          ftext("The measure of asymmetry in the distribution of a variable. Positive skewness indicates a long right tail, while negative skewness indicates a long left tail.", prop=fp_text(font.size=11)))
  )
  body_add_fpar(doc, fpar(ftext("Kurtosis: ", prop=fp_text( bold = TRUE, font.size=11)), 
                          ftext("The measure of the tail behavior of a distribution. Positive kurtosis signifies a distribution is more prone to outliers, and negative kurtosis implies a distribution is less prone to outliers.", prop=fp_text(font.size=11)))
  )
  body_add_par(doc, "")
  body_add_fpar(doc, fpar(ftext("Results", prop=fp_text( bold = TRUE, font.size=12))))
  
} else {
  doc = read_docx(paste(resultPath, "/Summary.docx", sep=""))
}


library(dplyr)
library(propagate) 
library(flextable) 

body_add_par(doc,"")
body_add_par(doc,paste("Filtered by: ", filterVar, "=", filterVal))
body_add_par(doc,"")
body_add_par(doc,paste("Variables: ", toString(indVars), "and" ,toString(depVars)))
body_add_par(doc,"")

myTable <- data.frame(Variable = as.character(),
                      Mean = as.character(),
                      n = as.character(),
                      SD = as.character(),
                      SE_M = as.character(), 
                      Min = as.character(),
                      Max = as.character(),
                      Skewness = as.character(), 
                      Kurtosis = as.character())
for(indVar in indVars){
  for (depVar in depVars) {
    mysummary <- group_by(my_data, my_data[[indVar]]) %>%
      summarise(
        count = n(),
        Avg = mean(!!rlang::sym(depVar), na.rm = TRUE),
        SD = sd(!!rlang::sym(depVar), na.rm = TRUE),
        Min= min(!!rlang::sym(depVar)),
        Max=max(!!rlang::sym(depVar)),
        Skewness = skewness(!!rlang::sym(depVar)), 
        Kurtosis = kurtosis(!!rlang::sym(depVar)), 
        SE_M = SD/sqrt(count)
      ) 
    myTable <- rbind(myTable, data.frame(Variable = depVar, Mean="", n = "", SD="", SE_M="", Min="", Max="", Skewness="", Kurtosis=""))
    
    for (row in 1:nrow(mysummary)) {
      indVarVal <- toString(mysummary[row, 1])
      count  <- mysummary[row, "count"]
      avg  <- specify_decimal(as.numeric(mysummary[row, "Avg"]), 2)
      sd  <- specify_decimal(as.numeric(mysummary[row, "SD"]), 2)
      se <- specify_decimal(as.numeric(mysummary[row, "SE_M"]), 2)
      min  <- specify_decimal(as.numeric(mysummary[row, "Min"]), 2)
      max  <- specify_decimal(as.numeric(mysummary[row, "Max"]), 2)
      skew  <- specify_decimal(as.numeric(mysummary[row, "Skewness"]), 2)
      kurt  <- specify_decimal(as.numeric(mysummary[row, "Kurtosis"]), 2)
      body_add_par(doc, "")
      text <- fpar(ftext(paste("For ", indVarVal, ", the observations of ", depVar, " had an average of ", avg, " (SD= ", sd,
                               ", SE", sep="")), 
                   ftext("M", prop=fp_text( bold = TRUE, font.size=11, vertical.align = "subscript")),
                   ftext(paste("=",se, ", Min = ", min, ", Max = ", max, ", Skewness = ", skew, ", Kurtosis = ", kurt, 
                               "). ", sep = ""))
                   )
      body_add_fpar(doc, (text))
      myTable <- rbind(myTable, data.frame(Variable = indVarVal, Mean=avg, n = as.character(count), SD=sd, SE_M=se, Min=min, Max=max, Skewness=skew, Kurtosis=kurt))
    }
    body_add_par(doc, "")
    
  }
}
body_add_par(doc, "")
body_add_caption(doc, block_caption(paste("Table showing summary statistics of ", toString(indVars), "vs", toString(depVars), "for", filterVar, "=", filterVal),
                                    style = "Table Caption"))
body_add_par(doc, "")
dat <- flextable(myTable)
dat <- compose(dat, i = 1, j = 5, part = "header",
               value = as_paragraph("SE", as_sub("M")) )
dat <- bg(dat, i = 1, bg="black", part = "header")
dat <- color(dat, i = 1, color="white", part = "header")
dat <- border(dat, border = fp_border(color = "black") )
body_add_flextable(doc, dat)
body_add_par(doc, "")
print(doc, target=paste(resultPath, "/Summary.docx", sep=""))

