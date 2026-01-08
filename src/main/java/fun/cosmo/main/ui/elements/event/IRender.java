package fun.cosmo.main.ui.elements.event;

import fun.cosmo.api.clientannotation.QuickImport;
import fun.cosmo.api.events.impl.Render2DEvent;

public interface IRender extends QuickImport {
    void onRender( Render2DEvent event);
}
