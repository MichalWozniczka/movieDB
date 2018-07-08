javac -cp lib/*: -d bin src/moviedb/DbBuilder.java
java -cp bin:lib/*: moviedb.DbBuilder
