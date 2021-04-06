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
filter2Var <- args[7]
filter2Val <- args[8]

#eval(parse(text=args[3]))

library(officer)
library(dplyr)
specify_decimal <- function(x, k) trimws(format(round(x, k), nsmall=k))
doc = NULL
if(!file.exists(paste(resultPath, "/Spearman Analysis.docx", sep=""))){
  doc = read_docx()
  body_add_fpar(doc, fpar(ftext("Spearman Correlation Analysis", prop=fp_text( bold = TRUE, font.size=14))))
  body_add_par(doc, "")
  body_add_fpar(doc, fpar(ftext("Introduction", prop=fp_text( bold = TRUE, font.size=12))))
  body_add_par(doc,"Cohen's standard was used to evaluate the strength of the relationships, where coefficients between .10 and .29 represent a small effect size, coefficients between .30 and .49 represent a moderate effect size, and coefficients above .50 indicate a large effect size (Cohen, 1988).",
               style = "Normal")
  body_add_par(doc,"", style="Normal")

} else {
  doc = read_docx(paste(resultPath, "/Spearman Analysis.docx", sep=""))
}
body_add_par(doc, "", style = "Normal")

my_data <- read.csv(dataPath)
fbText <- "Filtered by:"
if(filterVar != "none" && filterVal != "none" && !is.null(filterVal)){
  body_add_par(doc, "", style = "Normal")
  my_data <- subset(my_data, my_data[[filterVar]]==filterVal) # filtering by property value
  fbText <- paste(fbText, filterVar, "=", filterVal)
}
if(filter2Var != "none" && filter2Val != "none" && !is.null(filter2Val)){
  body_add_par(doc, "", style = "Normal")
  my_data <- subset(my_data, my_data[[filter2Var]]==filter2Val) # filtering by property value
  fbText <- paste(fbText, filter2Var, "=", filter2Val)
}
body_add_fpar(doc, fpar(ftext(fbText, prop=fp_text( bold = TRUE, font.size=12))))
body_add_par(doc,"", style="Normal")

myTable <- data.frame(Combination = as.character(),
                      r = as.character(),
                      Lower = as.character(),
                      Upper = as.character(), 
                      p = as.character())

vec <- unique(c(indVars, depVars))
print(vec)
df <- as.data.frame(combn(vec, 2, simplify = TRUE))
df <- t(df)
print(df)
body_add_fpar(doc, fpar(ftext("Assumptions", prop=fp_text( bold = TRUE, font.size=12))))
body_add_fpar(doc, fpar(ftext("Monotonic Relationship", prop=fp_text( bold = TRUE, font.size=12))))
body_add_par(doc, "A Spearman correlation requires that the relationship between each pair of variables does not change direction (Conover & Iman, 1981). This assumption is violated if the points on the scatterplot between any pair of variables appear to shift from a positive to negative or negative to positive relationship. Below are scatterplots of the correlations. A regression line has been added to assist the interpretation. ", style = "Normal")
body_add_par(doc, "")
library("ggpubr")
count = 1;
for(row in 1:nrow(df)){
  indVar <- df[row, 1]
  depVar <- df[row, 2]
  if(depVar != indVar){
    ymin <- min(as.numeric(my_data[[depVar]]))
    ymax <- max(as.numeric(my_data[[depVar]]))
    xmin <- min(as.numeric(my_data[[indVar]]))
    xmax <- max(as.numeric(my_data[[indVar]]))
    plot <- ggscatter(my_data, x = as.character(indVar), y = as.character(depVar), #mention data and axis 
                      add = "reg.line",  # Add regression line
                      ylim = c(ymin, ymax), xlim = c(xmin, xmax),
                      add.params = list(color = "red", fill = "lightgray"), # Customize regression line
                      conf.int = TRUE # Add confidence interval
    )+ stat_cor(method = "spearman", label.x = 3, label.y = 30) + 
      font("xlab", size = 8, face = "bold")+
      font("ylab", size = 8, face = "bold")+
      font("xy.text", size = 8)
    count = count + 1
    body_add_gg(doc, plot, width =3, height =3, style="Normal")
  }
}

body_add_fpar(doc, fpar(ftext("Results", prop=fp_text( bold = TRUE, font.size=12))))
body_add_par(doc, "")
body_add_par(doc, "The correlations were examined using Holm corrections to adjust for multiple comparisons based on an alpha value of 0.05.")
body_add_par(doc, "", style = "Normal")
corFound = FALSE
for(row in 1:nrow(df)){
  indVar <- df[row, 1]
  depVar <- df[row, 2]
  print("Vars")
  print(depVar)
  print(indVar)
  if(depVar != indVar){
    sp <- cor.test(my_data[[as.character(depVar)]], my_data[[as.character(indVar)]],  method = "spearman", adjust="Coehn", conf.level = 0.05)
    sp
    print(paste("p-value", as.double(sp$p.value)))
    print(paste("rho", as.double(sp$estimate)))
    estimate <- specify_decimal(as.numeric(sp$estimate), 4)
    pvalue <- specify_decimal(as.numeric(sp$p.value), 4)
    
    mdf <- data.frame(as.numeric(unlist(sp)))
    mdf
    
    library("RVAideMemoire")
    spc <- spearman.ci(my_data[[as.character(depVar)]], my_data[[as.character(indVar)]], conf.level = 0.95)
    print("Spearman")
    spc <- data.frame(as.numeric(unlist(spc)))
    
    print(paste("Lower", spc[6,1]))
    print(paste("Upper", spc[7,1]))
    lower <- specify_decimal(as.numeric(spc[6,1]), 3)
    upper <- specify_decimal(as.numeric(spc[7,1]), 3)
    
    relationType <- "positive"
    changeDep <- "increases"
    changeInd <- "increases"
    if(as.numeric(estimate) <0){
      relationType <- "negative"
      changeDep <- "decreases"
    }
    effectSize <- "no"
    if(as.numeric(estimate) > 0.5){
      effectSize <- "large"
    } else if(as.numeric(estimate) > 0.3 && as.numeric(estimate) < 0.49){
      effectSize <- "moderate"
    } else if(as.numeric(estimate) > 0.1 && as.numeric(estimate) < 0.29){
      effectSize <- "small"
    }
    if(pvalue <= 0.05){
      corFound = TRUE
      text <- paste("A significant", relationType ,"correlation was observed between", depVar, "and", indVar, " (r =", estimate, ", p =", pvalue,").",
                    "The correlation coefficient indicates a", effectSize ,"effect size.",
                    "This correlation indicates that as ", indVar, changeInd, depVar, "tends to", changeDep)
      body_add_par(doc,text, style="Normal")
    }

    myTable <- rbind(myTable, data.frame(Combination = paste(depVar, "-", indVar, sep = ""),
                                         r = estimate,
                                         Lower = lower,
                                         Upper = upper, 
                                         p = pvalue)
    )
  }
}

if(corFound){
  body_add_par(doc, "No other significant correlations were found.")
} else {
  body_add_par(doc, "There were no significant correlations between any pairs of variables.")
}
body_add_par(doc, "", style="Normal")
body_add_fpar(doc, fpar(ftext(paste("Spearman Correlation Results Among", toString(unique(c(indVars, depVars)))), prop=fp_text( italic = TRUE, font.size=11))))
body_add_par(doc, "", style="Normal")
body_add_table(doc, myTable, style = "Table Professional")
body_add_par(doc, "", style="Normal")
body_add_fpar(doc, fpar(ftext(paste("Note. The confidence intervals were computed using alpha = 0.05; Holm corrections used to adjust p-values.", toString(unique(indVars)), toString(unique(depVars))), prop=fp_text( italic = TRUE, font.size=11))))
body_add_par(doc, "", style="Normal")
body_add_par(doc, "", style="Normal")
print(doc, target=paste(resultPath, "/Spearman Analysis.docx", sep=""))
