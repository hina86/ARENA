requiredPackages = c('officer', 'dplyr', 'ggpubr', 'pgirmess', 'pastecs')
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

library("officer")
library("dplyr")
library("pastecs") 
require(ggpubr)
library(pgirmess)

specify_decimal <- function(x, k) trimws(format(round(x, k), nsmall=k))

doc=NULL
if(!file.exists(paste(resultPath, "/Kruskal Wallis.docx", sep=""))){
  doc = read_docx()
  body_add_fpar(doc, fpar(ftext("Kruskal-Wallis Test", prop=fp_text( bold = TRUE, font.size=14))))
  body_add_fpar(doc, fpar(ftext("Introduction", prop=fp_text( bold = TRUE, font.size=12))))
  body_add_par(doc, "A Kruskal-Wallis rank sum test was conducted to assess if there were significant differences in energy between the levels of library. The Kruskal-Wallis test is a non-parametric alternative to the one-way ANOVA and does not share the ANOVA's distributional assumptions (Conover & Iman, 1981).",
               style = "Normal")
  body_add_par(doc, "")
  body_add_fpar(doc, fpar(ftext("Results", prop=fp_text( bold = TRUE, font.size=12))))
} else {
  doc = read_docx(paste(resultPath, "/Kruskal Wallis.docx", sep=""))
}

my_data <- read.csv(dataPath)
#, header = TRUE,check.names = FALSE, colClasses = "character"
if(filterVal != "none"){
  my_data <- subset(my_data, my_data[[filterVar]]==filterVal) # filtering by property value
}

body_add_par(doc,"")
for(indVar in indVars){
  for (depVar in depVars) {
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
    if(as.numeric(p)> 0.5){
      sign <= ">"
      result = paste("indicating that the mean rank of",depVar,"was not significantly different between the levels of", indVar)
    } else {
      result = paste("indicating that the mean rank of",depVar,"was significantly different between the levels of", indVar)
    }
    body_add_par(doc,"")
    
    body_add_fpar(doc, fpar(ftext(paste(testMethod, "for", toString(depVar), "by", toString(indVar), ", filtered by", filterVar, "=", filterVal),
                                  prop=fp_text( bold = TRUE, font.size=12))
    )
    )
    body_add_par(doc,"")
    body_add_par(doc,paste("Sample size (N): ", nrow(my_data)))
    body_add_par(doc,"")
    
    text <- paste("The results of the Kruskal-Wallis test were significant based on an alpha value of 0.05, ",
                  "chi-squared(", df, ") =",specify_decimal(as.numeric(chisq), 3),", p=" , p, sign ,"0.5", 
                  result)
    body_add_par(doc, text)
    body_add_par(doc, "")#empty line
    body_add_caption(doc, block_caption(paste(testMethod, "for", indVar, "by", depVar), style = "Table Caption"))
    # creating a rank variable
    my_data$Rank <- rank(my_data[[depVar]])
    
    # getting the descriptive statistics for the groups
    by(my_data$Rank, my_data[[indVar]], stat.desc, basic = FALSE)
    
    
    dff <- group_by(my_data, my_data[[indVar]]) %>%
      summarise(
        MeanRank = mean(Rank)
      ) 
    dff
    dff <- setNames(dff, c(indVar, paste("Mean Rank of", depVar)))
    body_add_par(doc, "")
    body_add(doc, dff, style = "Table Professional")#table
    p <- ggboxplot(my_data, x=indVar, y="Rank") + 
      theme(axis.text.x = element_text(angle = 90, vjust = 0.5, hjust=1)) + 
      font("xlab", size = 8, face = "bold")+
      font("ylab", size = 8, face = "bold")+
      font("xy.text", size = 8)
    body_add_par(doc, "")#empty line
    body_add_gg(doc, p, width = 4, height = 4, style = "centered")
    body_add_caption(doc, block_caption(paste("Boxplot of", indVar, "vs ranks of", depVar), style = "Image Caption"))
    
    # post-hoc test for identifying statistical significant differences between the groups
    posthoc <- kruskalmc(my_data[[depVar]] ~ my_data[[indVar]], data = my_data)
    ph <- as.data.frame(posthoc)
    ph <- data.frame(indVar = rownames(ph), ph)
    pairs <- ""
    for (row in 1:nrow(ph)) {
      if(ph[row, 6] == TRUE){
        pairs <- paste(pairs, ph[row, 1], ",")
      }
    }
    if(pairs != ""){
      pairs <- substr(pairs, 0, nchar(pairs)-2)
    }
    ph <- subset(ph, select = c(1,4,5))
    ph <- setNames(ph, c("Comparison", "Observed Difference", "Critical Difference"))
    ph[,-1] <-round(ph[,-1],0) 
    body_add_par(doc, "")#empty line
    body_add_fpar(doc, fpar(ftext("Post Hoc Test", prop=fp_text( bold = TRUE, font.size=12))))
    body_add_par(doc, "")#empty line
    body_add_par(doc, paste("Pairwise comparisons were examined between each level of library. The results of the multiple comparisons indicated significant differences based on an alpha value of 0.05 between the following variable pairs: "))
    body_add_par(doc, pairs)
    body_add_par(doc, "")#empty line
    body_add_caption(doc, block_caption(paste("Pairwise Comparisons for the Mean Ranks of", indVar, "by Levels of", depVar), style = "Table Caption"))
    body_add_table(doc, ph, style = "Table Professional")
    body_add_par(doc, "Note. Observed Differences > Critical Differences indicate significance at the p < 0.05 level.")
    plot <- ggboxplot(my_data, x=indVar, y=depVar) + 
      theme(axis.text.x = element_text(angle = 90, vjust = 0.5, hjust=1)) + 
      font("xlab", size = 8, face = "bold")+
      font("ylab", size = 8, face = "bold")+
      font("xy.text", size = 8)
    body_add_par(doc, "")#empty line
    body_add_gg(doc, plot, width = 4, height = 4, style = "centered")
    body_add_caption(doc, block_caption(paste("Boxplot of", indVar, "vs", depVar), style = "Image Caption"))
    
    }
}
print(doc, target=paste(resultPath, "/Kruskal Wallis.docx", sep=""))


