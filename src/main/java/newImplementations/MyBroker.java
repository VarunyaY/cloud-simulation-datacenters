package newImplementations;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.CustomerEntity;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

public class MyBroker extends DatacenterBrokerSimple {
    private int lastSelectedVmIndex;
    private int lastSelectedDcIndex;
    private Vm lastSubmittedVm;
    private final List<Vm> vmWaitingList;
    private Datacenter lastSelectedDc;
    private Comparator<Vm> vmComparator;
    private BiFunction<Datacenter, Vm, Datacenter> datacenterMapper;
    private int vmCreationRequests;

    public MyBroker(final CloudSim simulation){
        this(simulation, "");
    }
    public MyBroker(CloudSim simulation, final String name) {
        super(simulation, name);
        this.lastSelectedVmIndex = -1;
        this.lastSelectedDcIndex = -1;
        this.vmWaitingList = new ArrayList<>();
        vmCreationRequests = 0;
    }

    @Override
    public Datacenter defaultDatacenterMapper(final Datacenter lastDatacenter, final Vm vm) {
        if(getDatacenterList().isEmpty()) {
            throw new IllegalStateException("You don't have any Datacenter created.");
        }

        if (lastDatacenter != Datacenter.NULL) {
            return getDatacenterList().get(lastSelectedDcIndex);
        }

        /*If all Datacenter were tried already, return Datacenter.NULL to indicate
         * there isn't a suitable Datacenter to place waiting VMs.*/
        if(lastSelectedDcIndex == getDatacenterList().size()-1){
            return Datacenter.NULL;
        }

        return getDatacenterList().get(++lastSelectedDcIndex);
    }

    @Override
    protected Vm defaultVmMapper(final Cloudlet cloudlet) {
        if (cloudlet.isBoundToVm()) {
            return cloudlet.getVm();
        }

        if (getVmExecList().isEmpty()) {
            return Vm.NULL;
        }

        /*If the cloudlet isn't bound to a specific VM or the bound VM was not created,
        cyclically selects the next VM on the list of created VMs.*/
        lastSelectedVmIndex = ++lastSelectedVmIndex % getVmExecList().size();
        return getVmFromCreatedList(lastSelectedVmIndex);
    }


    public DatacenterBroker submitVmList_new(final List<? extends Vm> list, Datacenter dc) {
        sortVmsIfComparatorIsSet(list);
        setBrokerForEntities(list);
        lastSubmittedVm = setIdForEntitiesWithoutOne(list, lastSubmittedVm);
        vmWaitingList.addAll(list);

        if (isStarted() && !list.isEmpty()) {
            LOGGER.info(
                    "{}: {}: List of {} VMs submitted to the broker during simulation execution. VMs creation request sent to Datacenter.",
                    getSimulation().clockStr(), getName(), list.size());
            requestDatacenterToCreateWaitingVms(true, dc);
        }
        //        val list = (1 to CLOUDLETS).map(c => new CloudletSimple(CLOUDLET_LENGTH, CLOUDLET_PES, utilizationModel)).toList
        return this;
    }

    private void sortVmsIfComparatorIsSet(final List<? extends Vm> list) {
        if (vmComparator != null) {
            list.sort(vmComparator);
        }
    }

    private void setBrokerForEntities(final List<? extends CustomerEntity> customerEntities) {
        for (final CustomerEntity entity : customerEntities) {
            entity.setBroker(this);
            if(entity instanceof VmGroup) {
                setBrokerForEntities(((VmGroup)entity).getVmList());
            }
        }
    }
    private <T extends CustomerEntity> T setIdForEntitiesWithoutOne(final List<? extends T> list, T lastSubmittedEntity) {
        return Simulation.setIdForEntitiesWithoutOne(list, lastSubmittedEntity);
    }

    private boolean requestDatacenterToCreateWaitingVms(final boolean isFallbackDatacenter, Datacenter dc) {
        for (final Vm vm : vmWaitingList) {
            this.lastSelectedDc = isFallbackDatacenter ?
                    defaultDatacenterMapper(dc, vm) :
                    datacenterMapper.apply(dc, vm);
            this.vmCreationRequests += requestVmCreation(lastSelectedDc, isFallbackDatacenter, vm);
        }

        return lastSelectedDc != Datacenter.NULL;
    }

    private int requestVmCreation(final Datacenter datacenter, final boolean isFallbackDatacenter, final Vm vm) {
        if (datacenter == Datacenter.NULL || datacenter.equals(vm.getLastTriedDatacenter())) {
            return 0;
        }

        logVmCreationRequest(datacenter, isFallbackDatacenter, vm);
        send(datacenter, vm.getSubmissionDelay(), CloudSimTags.VM_CREATE_ACK, vm);
        vm.setLastTriedDatacenter(datacenter);
        return 1;
    }

    private void logVmCreationRequest(final Datacenter datacenter, final boolean isFallbackDatacenter, final Vm vm) {
        final String fallbackMsg = isFallbackDatacenter ? " (due to lack of a suitable Host in previous one)" : "";
        if(vm.getSubmissionDelay() == 0)
            LOGGER.info(
                    "{}: {}: Trying to create {} in {}{}",
                    getSimulation().clockStr(), getName(), vm, datacenter.getName(), fallbackMsg);
        else
            LOGGER.info(
                    "{}: {}: Creation of {} in {}{} will be requested in {} seconds",
                    getSimulation().clockStr(), getName(), vm, datacenter.getName(),
                    fallbackMsg, vm.getSubmissionDelay());
    }
}
