FROM openjdk:17-jdk
WORKDIR /app
ENV BOOKS_DIRECTORY=/app/Datalake/eventstore/gutenbrg
ENV STOP_WORDS=/app/Stop_words.txt
ENV INDEX_DIRECTORY_TREE=/app/Datamarts/Inverted_Index/Tree_Data_Structure
ENV INDEX_DIRECTORY_UNIQUE=/app/Datamarts/Inverted_Index/Unique_Data_Structure
ENV INDEX_DIRECTORY_HIERARCHICAL=/app/Datamarts/Inverted_Index/Hierarchical_Data_Structure
ENV METADATA_DIRECTORY=/app/Datamarts/Metadata_Database
ENV PROCESSED_BOOKS_PATH=/app/Processed_books.txt

COPY target/indexer-1.0-SNAPSHOT.jar indexer.jar
CMD ["java", "-jar", "indexer.jar"]
