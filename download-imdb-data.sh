echo "Downloading IMDB dataset files..."
wget https://datasets.imdbws.com/title.basics.tsv.gz
wget https://datasets.imdbws.com/title.principals.tsv.gz
wget https://datasets.imdbws.com/title.ratings.tsv.gz
wget https://datasets.imdbws.com/title.akas.tsv.gz
wget https://datasets.imdbws.com/name.basics.tsv.gz
echo "Unzipping titles..."
gunzip title.basics.tsv.gz
echo "Unzipping principals..."
gunzip title.principals.tsv.gz
echo "Unzipping ratings..."
gunzip title.ratings.tsv.gz
echo "Unzipping akas..."
gunzip title.akas.tsv.gz
echo "Unzipping names..."
gunzip name.basics.tsv.gz
echo "Done!"
