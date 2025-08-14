package de.dtonal.stocktracker.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Repräsentiert die grundlegenden, statischen Informationen eines Wertpapiers
 * (z.B. einer Aktie).
 * Diese Daten müssen nicht für jeden Benutzer separat gespeichert werden, da
 * sie global gültig sind.
 */
public class CompanyProfile {
    private String country;
    private String currency;
    private String exchange;
    private String name;
    private String ticker;
    private String ipo;
    private double marketCapitalization;
    private double shareOutstanding;
    private String logo;
    private String phone;
    private String weburl;
    private String finnhubIndustry;
    private String isin;

    public String getCountry() {
        return country;
    }

    public String getCurrency() {
        return currency;
    }

    public String getExchange() {
        return exchange;
    }

    public String getName() {
        return name;
    }

    public String getTicker() {
        return ticker;
    }

    public String getIpo() {
        return ipo;
    }

    public double getMarketCapitalization() {
        return marketCapitalization;
    }

    public double getShareOutstanding() {
        return shareOutstanding;
    }

    public String getLogo() {
        return logo;
    }

    public String getPhone() {
        return phone;
    }

    public String getWeburl() {
        return weburl;
    }

    public String getFinnhubIndustry() {
        return finnhubIndustry;
    }

    public String getIsin() {
        return isin;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public void setIpo(String ipo) {
        this.ipo = ipo;
    }

    public void setMarketCapitalization(double marketCapitalization) {
        this.marketCapitalization = marketCapitalization;
    }

    public void setShareOutstanding(double shareOutstanding) {
        this.shareOutstanding = shareOutstanding;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setWeburl(String weburl) {
        this.weburl = weburl;
    }

    public void setFinnhubIndustry(String finnhubIndustry) {
        this.finnhubIndustry = finnhubIndustry;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    @Override
    public String toString() {
        return "CompanyProfile{" +
                "country='" + country + '\'' +
                ", currency='" + currency + '\'' +
                ", exchange='" + exchange + '\'' +
                ", name='" + name + '\'' +
                ", ticker='" + ticker + '\'' +
                ", ipo='" + ipo + '\'' +
                ", marketCapitalization=" + marketCapitalization +
                ", shareOutstanding=" + shareOutstanding +
                ", logo='" + logo + '\'' +
                ", phone='" + phone + '\'' +
                ", weburl='" + weburl + '\'' +
                ", finnhubIndustry='" + finnhubIndustry + '\'' +
                ", isin='" + isin + '\'' +
                '}';
    }


}
