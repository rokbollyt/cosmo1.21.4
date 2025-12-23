package ru.mytheria.main.ui.elements.event;

import ru.mytheria.api.clientannotation.QuickImport;
import ru.mytheria.api.events.impl.Render2DEvent;

public interface IRender extends QuickImport {
    void onRender( Render2DEvent event);
}
