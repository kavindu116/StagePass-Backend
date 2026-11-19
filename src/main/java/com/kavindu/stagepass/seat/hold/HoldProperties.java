package com.kavindu.stagepass.seat.hold;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConditionalOnProperty(prefix = "app.hold")
public record HoldProperties(@DefaultValue("10m")Duration ttl) {

}
