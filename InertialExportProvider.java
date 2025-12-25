package info.openrocket.core.simulation.extension.OpenRocketHITL;

import info.openrocket.core.plugin.Plugin;
import info.openrocket.core.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class InertialExportProvider extends AbstractSimulationExtensionProvider {
    public InertialExportProvider() {
        super(InertialExport.class, "Reports", "Inertial Data Export");
    }
}
