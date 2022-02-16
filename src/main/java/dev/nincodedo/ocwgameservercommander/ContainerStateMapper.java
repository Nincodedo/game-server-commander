package dev.nincodedo.ocwgameservercommander;

public class ContainerStateMapper {
    public ContainerState getContainerState(String state) {
        if (state == null) {
            return ContainerState.UNKNOWN;
        }

        if (state.equals("exited")) {
            return ContainerState.EXITED;
        }

        return ContainerState.UNKNOWN;
    }
}
