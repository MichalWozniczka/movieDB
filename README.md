# About

Builds a postgreSQL database of films, IMDb/Rotten Tomatoes ratings, film cast and crew, and other information. This database is first constructed from the IMDb dataset .tsv's (https://www.imdb.com/interfaces/), and then the RT ratings are scraped from rottentomatoes.com. The webscraping is done using jsoup to send URLs pretending to be RT's search engine and to retrieve the resulting HTML file, and json-simple to parse through the json file holding movie information that is contained in the HTML.

# Database Schema

```
films(film_id (char(9)), title (String), year (Integer), runtime (Integer), genres (String[]), crew_ids (String[]), imdb (Double), imdb_numvotes (Integer), rt (Double), rt_numvotes (Integer), regions (String[]))
cast_and_crew(name_id (char(9)), name (String), titles (String[]))
```

# Sample Datasets

Included in the repository is a subset of the IMDb datasets with films only from 2017 and cast and crew that only appears in films from 2017. This subset was created using `createSmallDataset.py` in `imdb-dataset-subset-2017`. `CreateSmallDataset.py` reads the full-sized dataset in the root-level directory and writes the subset dataset in `imdb-dataset-subset-2017`.

To use the full-sized dataset from IMDb (https://www.imdb.com/interfaces/), run `source download-full-imdb-datasets.sh`. This will download the datasets from IMDb and unzip them. Careful, because the full-sized dataset is over a gigabyte!

To use the 2017 subset of the dataset, run `source use-imdb-2017-subset.sh`. This simply copies the .tsv files from `imdb-dataset-subset-2017`; however, it will delete any existing .tsv files in the root directory, so if you want to use the full-size dataset you have to run `source download-full-imdb-datasets.sh` again.

# How to run:

```
javac -cp lib/*: -d bin src/moviedb/DbBuilder.java
java -cp bin:lib/*: moviedb.DbBuilder
```

Or, more simply,

```
source build-db.sh
```

# Technologies

Written in Java
Database used: PostgreSQL
