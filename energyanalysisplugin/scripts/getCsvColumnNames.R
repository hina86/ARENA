args <- commandArgs(trailingOnly = TRUE)
df <- read.csv(file=args[1],nrows=1)
cols <- colnames(df, do.NULL = FALSE, prefix = "")
cat(paste(paste(cols, collapse= "\n"), "\n"))