package info.openrocket.core.simulation.extension.OpenRocketHITL;

import java.io.File;
import java.io.PrintStream;
import java.lang.Exception;

import javax.swing.JOptionPane;

import info.openrocket.core.simulation.extension.AbstractSimulationExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.SimulationConditions;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.AbstractSimulationListener;

public class InertialExport extends AbstractSimulationExtension {
    private static final Logger log = LoggerFactory.getLogger(InertialExport.class);

    private static enum Types {
        t {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getSimulationTime();
            }
        },
        relPosX {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getRocketPosition().getX();
            }
        },
        relPosY {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getRocketPosition().getY();
            }
        },
        relPosZ {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getRocketPosition().getZ();
            }
        },
        lat {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getRocketWorldPosition().getLatitudeDeg();
            }
        },
        lon {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getRocketWorldPosition().getLongitudeDeg();
            }
        },
        worldVelX {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getRocketVelocity().getX();
            }
        },
        worldVelY {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getRocketVelocity().getY();
            }
        },
        worldVelZ {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getRocketVelocity().getZ();
            }
        },
        worldAccX {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getFlightDataBranch().getLast(FlightDataType.TYPE_ACCELERATION_X);
            }
        },
        worldAccY {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getFlightDataBranch().getLast(FlightDataType.TYPE_ACCELERATION_Y);
            }
        },
        worldAccZ {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getFlightDataBranch().getLast(FlightDataType.TYPE_ACCELERATION_Z);
            }
        },
        qW {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getRocketOrientationQuaternion().getW();
            }
        },
        qX {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getRocketOrientationQuaternion().getX();
            }
        },
        qY {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getRocketOrientationQuaternion().getY();
            }
        },
        qZ {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getRocketOrientationQuaternion().getZ();
            }
        },
        bodyAccX {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getFlightDataBranch().getLast(FlightDataType.TYPE_ACCELERATION_BODYX);
            }
        },
        bodyAccY {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getFlightDataBranch().getLast(FlightDataType.TYPE_ACCELERATION_BODYY);
            }
        },
        bodyAccZ {
            @Override
            public double getValue(SimulationStatus status) {

                return status.getFlightDataBranch().getLast(FlightDataType.TYPE_ACCELERATION_BODYZ);
            }
        },
        bodyGyrX {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getFlightDataBranch().getLast(FlightDataType.TYPE_YAW_RATE);
            }
        },
        bodyGyrY {
            @Override
            public double getValue(SimulationStatus status) {
                return status.getFlightDataBranch().getLast(FlightDataType.TYPE_PITCH_RATE);
            }
        },
        bodyGyrZ {
            @Override
            public double getValue(SimulationStatus status) {

                return status.getFlightDataBranch().getLast(FlightDataType.TYPE_ROLL_RATE);
            }
        }
        ;
        public abstract double getValue(SimulationStatus status);
    }

    // format of CSV file name
    public static final String FILENAME_FORMAT = "simulation-%03d.csv";

    private PrintStream output = null;

    // Description shown when blue "i" button is clicked
    @Override
    public String getDescription() {
        return "Exports inertial and global position/velocity/acceleration and orientation/rates data to a CSV file in the source root directory";
    }

    // 
    @Override
    public void initialize(SimulationConditions conditions) throws SimulationException {
        conditions.getSimulationListenerList().add(new InertialExport.InertialExportListener());
    }

    // The actual listener that does all the work
    private class InertialExportListener extends AbstractSimulationListener {

        // when we start the simulation we'll create the .csv file and write the column headings
        @Override
        public void startSimulation(SimulationStatus status) throws SimulationException {
            // Just in case a prior run somehow failed to close the file.
            if (output != null) {
                log.warn("WARNING: Ending simulation logging to CSV file " +
                        "(SIMULATION_END not encountered).");
                output.close();
                output = null;
            }

            File file = null;
            try {
                // Construct filename, atomically create and open file
                int n = 1;
                do {
                    file = new File(String.format(FILENAME_FORMAT, n));
                    n++;
                } while (!file.createNewFile());
                log.info("CSV file name is " + file.getName());

                output = new PrintStream(file);

                // Write column headers
                final InertialExport.Types[] types = InertialExport.Types.values();
                StringBuilder s = new StringBuilder(types[0].toString());
                for (int i = 1; i < types.length; i++) {
                    s.append("," + types[i].toString());
                }

                output.println(s);
                output.flush();

            } catch (Exception e) {
                log.error("ERROR OPENING FILE: " + e);
                JOptionPane.showMessageDialog(null,
                        "Error Opening File:\n" + e.getMessage(),
                        "Error Opening File: " + file,
                        JOptionPane.ERROR_MESSAGE);
            }

        }

//        // Log an event
//        @Override
//        public boolean handleFlightEvent(SimulationStatus status, FlightEvent event) throws SimulationException {
//
//            // ALTITUDE events are special in the sense that they are just used to schedule simulation steps.
//            // We want to hear about all the others
//            if ((null != output) && (event.getType() != FlightEvent.Type.ALTITUDE)) {
//                log.info("logging event " + event + " to CSV file");
//                output.println("# Event " + event);
//                output.flush();
//            }
//
//            return true;
//        }

        // Log data at end of sim step.
        @Override
        public void postStep(SimulationStatus status) throws SimulationException {

            final InertialExport.Types[] types = InertialExport.Types.values();
            StringBuilder s;

            if (output != null) {
                log.info("logging data to CSV file");
                s = new StringBuilder("" + types[0].getValue(status));
                for (int i = 1; i < types.length; i++) {
                    s.append("," + types[i].getValue(status));
                }
                output.println(s);
                output.flush();

            }
        }

        // Close log file at end of simulation
        @Override
        public void endSimulation(SimulationStatus status, SimulationException exception) {
            if (output != null) {
                log.info("Closing CSV file");
                output.close();
                output = null;
            }
        }
    }
}
