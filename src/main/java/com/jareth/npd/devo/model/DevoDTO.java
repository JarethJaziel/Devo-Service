package com.jareth.npd.devo.model;

import lombok.Data;

@Data
public class DevoDTO {
    private Devo data;
    private String formatWhatsapp;
    private String formatWeb;

    public DevoDTO(Devo data, String formatWhatsapp, String formatWeb) {
        this.data = data;
        this.formatWhatsapp = formatWhatsapp;
        this.formatWeb = formatWeb;
    }

}