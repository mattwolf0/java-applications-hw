package javaapplications.hw;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ForexTradeService {

    private final List<ForexTrade> trades = new ArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public ForexTrade openTrade(String instrument, BigDecimal units, BigDecimal price) {
        if (instrument == null || instrument.isBlank()) {
            throw new IllegalArgumentException("Instrument megadása kötelező.");
        }
        if (units == null || units.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("A mennyiség nem lehet nulla.");
        }
        if (price == null) {
            throw new IllegalArgumentException("Ár megadása kötelező.");
        }

        ForexTrade trade = new ForexTrade();
        trade.setId(idSequence.getAndIncrement());
        trade.setInstrument(instrument);
        trade.setUnits(units);
        trade.setOpenPrice(price);
        trade.setOpenTime(OffsetDateTime.now());
        trade.setClosed(false);

        synchronized (trades) {
            trades.add(trade);
        }
        return trade;
    }

    public List<ForexTrade> getOpenTrades() {
        synchronized (trades) {
            return trades.stream()
                    .filter(t -> !t.isClosed())
                    .collect(Collectors.toList());
        }
    }

    public ForexTrade getTradeById(long id) {
        synchronized (trades) {
            return trades.stream()
                    .filter(t -> t.getId() == id)
                    .findFirst()
                    .orElse(null);
        }
    }

    public ForexTrade closeTrade(long id, BigDecimal closePrice) {
        if (closePrice == null) {
            throw new IllegalArgumentException("Ár megadása kötelező.");
        }

        ForexTrade trade = getTradeById(id);
        if (trade == null) {
            throw new IllegalArgumentException("Nincs ilyen trade azonosító.");
        }
        if (trade.isClosed()) {
            throw new IllegalStateException("A pozíció már zárva van.");
        }

        trade.setClosePrice(closePrice);
        trade.setCloseTime(OffsetDateTime.now());
        trade.setClosed(true);
        return trade;
    }
}
