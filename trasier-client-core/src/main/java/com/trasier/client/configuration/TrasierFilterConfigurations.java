package com.trasier.client.configuration;

import lombok.Data;

@Data
public class TrasierFilterConfigurations {

    private TrasierFilterConfiguration allow = new TrasierFilterConfiguration();
    private TrasierFilterConfiguration cancel = new TrasierFilterConfiguration();
    private TrasierFilterConfiguration disablePayload = new TrasierFilterConfiguration();

}
