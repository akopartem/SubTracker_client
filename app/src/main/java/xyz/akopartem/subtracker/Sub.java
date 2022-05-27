package xyz.akopartem.subtracker;

import java.time.LocalDate;
import java.util.Date;

public class Sub {
    String name;
    int price;
    LocalDate date;
    public Sub(String name, int price, LocalDate date) {
        this.name = name;
        this.price = price;
        this.date = date;
    }

    @Override
    public String toString() {
        return "Sub{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", date=" + date +
                '}';
    }
}
