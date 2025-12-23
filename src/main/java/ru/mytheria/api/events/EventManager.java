package ru.mytheria.api.events;



import ru.mytheria.Mytheria;

public class EventManager {

    public static void register(Class<?> clazz) {
        Mytheria.getInstance().getEventProvider().subscribe(clazz);
    }

    public static void call( Event eventLayer) {
        Mytheria.getInstance().getEventProvider().post(eventLayer);
    }

    public static void cancel( Event eventLayer) {
        eventLayer.cancel();
    }

    public static boolean isCancel( Event eventLayer) {
        return eventLayer.isCanceled();
    }

}
