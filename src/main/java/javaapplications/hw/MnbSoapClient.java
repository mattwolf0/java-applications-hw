package javaapplications.hw;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MnbSoapClient {

    private static final String ENDPOINT = "https://www.mnb.hu/arfolyamok.asmx";

    /**
     * Árfolyam lekérdezés az MNB SOAP webservice-ből.
     *
     * @param startDate  "YYYY-MM-DD"
     * @param endDate    "YYYY-MM-DD"
     * @param currency   pl. "EUR"
     */
    public List<ExchangeRatePoint> getExchangeRates(String startDate,
                                                    String endDate,
                                                    String currency) {
        try {
            String soapRequestXml = buildSoapRequest(startDate, endDate, currency);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.add("SOAPAction", "/webservices/MNBArfolyamServiceSoap/GetExchangeRates");

            HttpEntity<String> request = new HttpEntity<>(soapRequestXml, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response =
                    restTemplate.postForEntity(ENDPOINT, request, String.class);

            String body = response.getBody();
            if (body == null || body.isBlank()) {
                throw new IllegalStateException("Üres válasz érkezett az MNB-től.");
            }

            String innerXml = extractResultXml(body);
            return parseExchangeRates(innerXml, currency);

        } catch (Exception e) {

            throw new RuntimeException("Hiba az MNB SOAP hívás során: " + e.getMessage(), e);
        }
    }

    private String buildSoapRequest(String startDate,
                                    String endDate,
                                    String currency) {
        //YYYY-MM-DD
        return
                """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                  xmlns:web="http://www.mnb.hu/webservices/">
                  <soapenv:Header/>
                  <soapenv:Body>
                    <web:GetExchangeRates>
                      <web:startDate>%s</web:startDate>
                      <web:endDate>%s</web:endDate>
                      <web:currencyNames>%s</web:currencyNames>
                    </web:GetExchangeRates>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(startDate, endDate, currency);
    }


    private String extractResultXml(String soapResponse) {
        String startTag = "<GetExchangeRatesResult>";
        String endTag = "</GetExchangeRatesResult>";

        int start = soapResponse.indexOf(startTag);
        int end = soapResponse.indexOf(endTag);

        if (start == -1 || end == -1) {
            throw new IllegalStateException("Nem található GetExchangeRatesResult a válaszban.");
        }

        start += startTag.length();
        return soapResponse.substring(start, end);
    }

    /**
     * Inner XML feldolgozása:
     * <MNBExchangeRates>
     *   <Day date="2024-01-02">
     *     <Rate curr="EUR" unit="1">392,45</Rate>
     *   </Day>
     *   ...
     * </MNBExchangeRates>
     */
    private List<ExchangeRatePoint> parseExchangeRates(String resultXml,
                                                       String currency) throws Exception {
        List<ExchangeRatePoint> result = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(resultXml)));

        NodeList dayNodes = doc.getElementsByTagName("Day");
        for (int i = 0; i < dayNodes.getLength(); i++) {
            Element dayEl = (Element) dayNodes.item(i);
            String dateStr = dayEl.getAttribute("date");
            LocalDate date = LocalDate.parse(dateStr);

            NodeList rateNodes = dayEl.getElementsByTagName("Rate");
            for (int j = 0; j < rateNodes.getLength(); j++) {
                Element rateEl = (Element) rateNodes.item(j);
                String curr = rateEl.getAttribute("curr");
                if (!currency.equalsIgnoreCase(curr)) {
                    continue;
                }

                String unitStr = rateEl.getAttribute("unit");
                String valueStr = rateEl.getTextContent().trim();
                // , to .
                valueStr = valueStr.replace(',', '.');

                BigDecimal rawValue = new BigDecimal(valueStr);
                BigDecimal unit = new BigDecimal(unitStr);

                BigDecimal perOne = rawValue
                        .divide(unit, 4, RoundingMode.HALF_UP);

                result.add(new ExchangeRatePoint(date, perOne));
            }
        }

        return result;
    }
}
