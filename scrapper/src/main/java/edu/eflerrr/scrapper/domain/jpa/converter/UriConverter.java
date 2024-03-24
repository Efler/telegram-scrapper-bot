package edu.eflerrr.scrapper.domain.jpa.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.net.URI;

@Converter
public class UriConverter implements AttributeConverter<URI, String> {

    @Override
    public String convertToDatabaseColumn(URI url) {
        if (url == null) {
            return null;
        }
        return url.toString();
    }

    @Override
    public URI convertToEntityAttribute(String dbUrl) {
        if (dbUrl == null || dbUrl.isEmpty()) {
            return null;
        }
        return URI.create(dbUrl);
    }

}
