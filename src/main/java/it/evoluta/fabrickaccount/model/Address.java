package it.evoluta.fabrickaccount.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
@Getter
@Setter
public class Address implements Serializable {

    public String address;

    public String city;

    public String countryCode;
}
