package com.skytracker.elasticsearch.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

@Document(indexName = "search-log")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SearchLogsDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String originLocationCode;

    @Field(type = FieldType.Keyword)
    private String destinationLocationCode;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate departureDate;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate returnDate;
}
