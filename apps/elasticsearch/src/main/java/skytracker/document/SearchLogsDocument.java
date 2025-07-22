package skytracker.document;

import com.skytracker.dto.FlightSearchRequestDto;
import com.skytracker.dto.SearchLogDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(indexName = "search-log")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class SearchLogsDocument {

    @Id
    private String id;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime timestamp;

    @Field(type = FieldType.Keyword)
    private String originLocationCode;

    @Field(type = FieldType.Keyword)
    private String destinationLocationCode;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate departureDate;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate returnDate;
}
