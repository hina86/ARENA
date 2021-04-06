args <- commandArgs(trailingOnly = TRUE)
df <- read.csv(file=args[1])[, c(args[2])]
uniqueVals <- unique(df)
cat(paste(paste(uniqueVals, collapse= "\n"), "\n"))