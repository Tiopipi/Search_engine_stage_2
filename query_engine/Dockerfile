FROM openjdk:17-jdk
WORKDIR /app
ENV METADATA_FILE_PATH=/app/Datamarts/Metadata_Database/metadata.csv
ENV TREE_DIRECTORY=/app/Datamarts/Inverted_Index/Tree_Data_Structure
ENV UNIQUE_FILE_PATH=/app/Datamarts/Inverted_Index/Unique_Data_Structure/invertedIndex.json
ENV HIERARCHICAL_DIRECTORY=/app/Datamarts/Inverted_Index/Hierarchical_Data_Structure
ENV BOOKS_FILE_PATH=/app/Datalake/eventstore/gutenbrg
COPY src/main/resources/public /app/public
COPY target/query_engine-1.0-SNAPSHOT.jar query_engine.jar
EXPOSE 8080
CMD ["bash", "-c", "java -jar query_engine.jar"]
