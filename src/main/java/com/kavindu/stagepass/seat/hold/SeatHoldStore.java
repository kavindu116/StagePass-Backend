package com.kavindu.stagepass.seat.hold;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class SeatHoldStore {

    private static final String SEAT_KEY = "stagepass:seat:";
    private static final String HOLD_KEY = "stagepass:hold:";

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<Long> holdScript;
    private final DefaultRedisScript<Long> releaseScript;


    public SeatHoldStore(StringRedisTemplate redis) {
        this.redis = redis;
        this.holdScript = script("scripts/hold_seats.lua");
        this.releaseScript = script("scripts/release_hold.lua");
    }

    public Optional<Long> tryHold(Long showId, List<Long> seatIds, String holdKey, Duration ttl) {
        List<String> keys = keys(holdKey, seatIds);
        String payload = showId + ":" + seatIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        Long result = redis.execute(holdScript,keys,holdKey,String.valueOf(ttl.toMillis()),payload);

        if (result == null || result == 0L){
            return Optional.empty();
        }

        return Optional.of(seatIds.get(result.intValue()-1));
    }

    public Set<Long> heldAmong(Collection<Long> seatIds){
       if (seatIds.isEmpty()){
           return Set.of();
       }

       List<Long> ids = new ArrayList<>(seatIds);
       List<String> values = redis.opsForValue().multiGet(ids.stream().map(id -> SEAT_KEY + id).toList());

       Set<Long> held = new HashSet<>();
       for (int i=0; i<ids.size(); i++){
           if (values != null && values.get(i) != null){
               held.add(ids.get(i));
           }
       }
       return held;
    }

    public Optional<Hold> find(String holdId){
        String payload = redis.opsForValue().get(HOLD_KEY + holdId);
        Long ttlMillis = redis.getExpire(HOLD_KEY + holdId, TimeUnit.MILLISECONDS);

        if (payload == null || ttlMillis == null || ttlMillis <= 0){
            return Optional.empty();
        }

        String[] parts = payload.split(":",2);
        List<Long> seatIds = Arrays.stream(parts[1].split(",")).map(Long::valueOf).toList();
        return Optional.of(new Hold(holdId,Long.valueOf(parts[0]),seatIds,Duration.ofMillis(ttlMillis)));
    }

    public long release(Hold hold){
        Long released = redis.execute(releaseScript,keys(hold.holdId(),hold.seatIds()),hold.holdId());
        return released == null ? 0L : released;
    }

    public record Hold(String holdId, Long showId, List<Long> seatIds, Duration remaining) {
    }


    private static List<String> keys(String holdId,List<Long> seatIds) {
        List<String> keys = new ArrayList<>(seatIds.size()+1);
        keys.add(HOLD_KEY +  holdId);
        seatIds.forEach(id -> keys.add(SEAT_KEY +  id));
        return keys;
    }


    private static DefaultRedisScript<Long> script(String path) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(path)));
        script.setResultType(Long.class);
        return script;
    }
}
