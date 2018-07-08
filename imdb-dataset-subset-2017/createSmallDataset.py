savedTitles = {}

def saveRelevantTitles(fileName):
    #save IDs of titles in 2017 and store the corresponding lines
    titles = open("../"+fileName, "r")
    titlesLines = titles.readlines()
    subsetTitles = open(fileName, "w+")
    subsetTitles.write(titlesLines[0])
    for line in titlesLines:
        splitLine = line.split("\t")
        if splitLine[5] == "2017" and splitLine[1] == "movie":
            subsetTitles.write(line)
            savedTitles[splitLine[0]] = True

def saveRelevantTitleInfo(fileName):
    #store lines that are about titles from 2017
    full = open("../"+fileName, "r")
    fullLines = full.readlines()
    subset = open(fileName, "w+")
    subset.write(fullLines[0])
    for line in fullLines:
        splitLine = line.split("\t")
        if splitLine[0] in savedTitles:
            subset.write(line)

def saveRelevantNames(fileName):
    #store lines with actors that appeared in films from 2017
    full = open("../"+fileName, "r")
    fullLines = full.readlines()
    subset = open(fileName, "w+")
    subset.write(fullLines[0])
    for line in fullLines:
        splitLine = line.split("\t")
        knownForTitles = splitLine[5].split(",")
        lineWritten = False
        for title in knownForTitles:
            if lineWritten:
                continue
            if title in savedTitles:
                subset.write(line)
                lineWritten = True

def main():
    saveRelevantTitles("title.basics.tsv")

    saveRelevantTitleInfo("title.akas.tsv")
    saveRelevantTitleInfo("title.principals.tsv")
    saveRelevantTitleInfo("title.ratings.tsv")

    saveRelevantNames("name.basics.tsv")


if __name__ == "__main__":
    main()
