package simulations

/*
 * CloudSim Plus: A modern, highly-extensible and easier-to-use Framework for
 * Modeling and Simulation of Cloud Computing Infrastructures and Services.
 * http://cloudsimplus.org
 *
 *     Copyright (C) 2015-2018 Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Plus.
 *
 *     CloudSim Plus is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Plus. If not, see <http://www.gnu.org/licenses/>.
 */

import org.cloudbus.cloudsim.brokers.{DatacenterBrokerAbstract, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.resources.Pe
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.{Vm, VmCost, VmSimple}
import org.cloudsimplus.builders.tables.{CloudletsTableBuilder, TextTableColumn}
import java.util

import com.typesafe.config.{Config, ConfigFactory}
import newImplementations.MyBroker
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicy, VmAllocationPolicyAbstract, VmAllocationPolicyBestFit, VmAllocationPolicyRoundRobin, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletScheduler, CloudletSchedulerAbstract, CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.{VmSchedulerSpaceShared, VmSchedulerTimeShared}
import org.slf4j.{Logger, LoggerFactory}
import simulations.Simulation_part5.cloudletSchedulerType

import scala.jdk.javaapi.CollectionConverters.asJava
import scala.jdk.CollectionConverters._

/**
 * A minimal but organized, structured and re-usable CloudSim Plus example
 * which shows good coding practices for creating simulation scenarios.
 *
 * <p>It defines a set of constants that enables a developer
 * to change the number of Hosts, VMs and Cloudlets to create
 * and the number of {@link Pe}s for Hosts, VMs and Cloudlets.</p>
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 1.0
 */


/*
Simulation 5 - A simulation with 3 datacenters to serve the different services
 */

object Simulation_part5 {

  println("Please enter the details as prompted. The details help to decide on the type of service to be provided.")

//  Code block to accept the user inputs to decide on the service type
  println("Provide cloudlet number?(0-if not necessary)")
  val cloudletNumber = scala.io.StdIn.readInt()
  println("Provide cloudlet length?(0-if not necessary")
  val cloudletLength = scala.io.StdIn.readInt()
  println("Provide cloudlet PEs?(0-if not necessary)")
  val cloudletPEs = scala.io.StdIn.readInt()
  println("Provide cloudlet scheduler policy? 1.TimeShared  2.SpaceShared  3.NA")
  val cloudletSchedulerType = scala.io.StdIn.readInt()
  println("Provide operating system? 1.Debian 2.Ubuntu 3.NA")
  val operatingSystem = scala.io.StdIn.readInt()
  println("Provide VM allocation policy? 1.RoundRobin  2.Bestfit  3.NA")
  val vmAllocationPolicy = scala.io.StdIn.readInt()

//  Getting the rest of the parameters from the configuration file
  val SIM = "Simulation_part5";
  val conf: Config = ConfigFactory.load(SIM + ".conf")
  val VMS: Int = conf.getInt(SIM + "." + "numVMs")
  val VM_PES: Int = conf.getInt(SIM + "." + "vm" + ".numberPES")

  val simulation = new CloudSim

  val broker0 = new DatacenterBrokerSimple(simulation)
//  val broker_new = new MyBroker(simulation)
//  val dc4 = createDatacenter(conf.getInt(SIM + "." + "datacenter2" + ".numberHosts"), conf.getInt(SIM + "." + "host" + ".pes")
//    ,getVMAllocationPolicy(vmAllocationPolicy))

  if(getOperatingSystemType(operatingSystem)!="NA"){
//      Iaas
    createDatacenter(conf.getInt(SIM + "." + "datacenter1" + ".numberHosts"), conf.getInt(SIM + "." + "host" + ".pes")
    , getVMAllocationPolicy(vmAllocationPolicy), "datacenter1")
  }
  else{
    if(getCloudletSchedulerType(cloudletSchedulerType)!=null){
//      Paas
      createDatacenter(conf.getInt(SIM + "." + "datacenter2" + ".numberHosts"), conf.getInt(SIM + "." + "host" + ".pes")
      ,getVMAllocationPolicy(vmAllocationPolicy), "datacenter2")
    }
    else{
      if(cloudletNumber!=0 && cloudletPEs!=0)
//      Saas
      createDatacenter(conf.getInt(SIM + "." + "datacenter0" + ".numberHosts"), conf.getInt(SIM + "." + "host" + ".pes")
      , getVMAllocationPolicy(vmAllocationPolicy), "datacenter0")
      else{
//      Faas
        createDatacenter(conf.getInt(SIM + "." + "datacenter0" + ".numberHosts"), conf.getInt(SIM + "." + "host" + ".pes")
          ,getVMAllocationPolicy(vmAllocationPolicy), "datacenter0")
      }
    }
  }
  //  Assigning values from the configuration file if the user hasn't provided them
  val CLOUDLETS: Int = if(cloudletNumber==0)conf.getInt(SIM + "." + "numCloudlets") else cloudletNumber
  val CLOUDLET_PES: Int = if(cloudletPEs==0)conf.getInt(SIM + "." + "cloudlet" + ".numberPES") else cloudletPEs
  val CLOUDLET_LENGTH: Int = if(cloudletLength==0)conf.getInt(SIM + "." + "cloudlet" + ".length") else cloudletLength
  val LOG: Logger = LoggerFactory.getLogger(getClass)

  //  val HOSTS: Int = conf.getInt(SIM + "." + "datacenter0" + ".numberHosts")
  //  val HOST_PES: Int = conf.getInt(SIM + "." + "host" + ".pes")

  //  val datacenter0: DatacenterSimple = createDatacenter(HOSTS)
  //  //<<>>Creates a broker that is a software acting on behalf a cloud customer to manage his/her VMs and Cloudlets
  //  //<<>> val broker0 = new DatacenterBrokerSimple(simulation)
  val vmList: util.List[Vm] = createVms(getCloudletSchedulerType(cloudletSchedulerType))
  val cloudletList: util.List[CloudletSimple] = createCloudlets

  broker0.submitVmList(vmList)
  broker0.submitCloudletList(cloudletList)

  simulation.start
  val finishedCloudlets: util.List[Cloudlet] = broker0.getCloudletFinishedList

  new CloudletsTableBuilder(finishedCloudlets).build()
  println("---------------")
  println(CLOUDLETS*finishedCloudlets.get(0).getTotalCost)
  LOG.info("Simulation completed")

  def getOperatingSystemType(operatingSystem: Int): String ={
    operatingSystem match{
      case 1 => "Debian"
      case 2 => "Ubuntu"
      case _ => "NA"
    }
  }

  def getCloudletSchedulerType(cloudletSchdulerType: Int):CloudletScheduler= {
    cloudletSchdulerType match{
      case 1 => new CloudletSchedulerTimeShared
      case 2 => new CloudletSchedulerSpaceShared
      case _ => null
    }
  }

  def getVMAllocationPolicy(vmAllocationPolicy: Int):VmAllocationPolicy = {
    vmAllocationPolicy match{
      case 1 => new VmAllocationPolicyRoundRobin
      case 2 => new VmAllocationPolicyBestFit
      case _ => null
    }
  }


  /*
   * To create a Datacenter for the simulation to check the various parameters
   */
  def createDatacenter(numberHosts: Int, hostPES: Int, vmAllocationPolicyType: VmAllocationPolicy, dcString : String): DatacenterSimple = {
    val hostList_new = (1 to numberHosts).map(host => createHost(hostPES)).toList

    //    Uses a VmAllocationPolicySimple by default to allocate VM
    val dc = new DatacenterSimple(simulation, hostList_new.asJava,
      if(vmAllocationPolicyType !=null) vmAllocationPolicyType
      else new VmAllocationPolicySimple)
    dc.getCharacteristics()
      .setCostPerSecond(conf.getInt(SIM + "." + dcString + ".cost"))
      .setCostPerBw(conf.getInt(SIM + "." + dcString + ".costPerBw"))
      .setCostPerMem(conf.getInt(SIM + "." + dcString +  ".costPerMem"))
      .setCostPerStorage(conf.getInt(SIM + "." + dcString +  ".costPerStorage"))
    dc
  }

  /*
   Method to create a list of Hosts in a datacenter.
   */
  def createHost(hostPES: Int): Host = {
    val peList = (1 to hostPES).map(pe => new PeSimple(1000)).toList
    val ram = conf.getInt(SIM + "." + "host" + ".ram") //in Megabytes
    val bw = conf.getInt(SIM + "." + "host" + ".bw") //in Megabits/s
    val storage = conf.getInt(SIM + "." + "host" + ".storage")
    new HostSimple(ram, bw, storage, asJava[Pe](peList))
  }

  /*
  Method to create a list of VMs with default TimeShared scheduler.
  */
  def createVms(cloudletSchedulerType:CloudletScheduler): util.List[Vm] = {
    val list = (1 to VMS).map(vm => new VmSimple(1000, VM_PES)
       .setCloudletScheduler(if(cloudletSchedulerType == null) new CloudletSchedulerTimeShared
       else cloudletSchedulerType)).toList
    list.asJava
  }

  /*
  Method to create a list of Cloudlets with Full Utlization model
  */
  def createCloudlets: util.List[CloudletSimple] = {
    //  UtilizationModel defining the Cloudlets use only 50% of any resource all the time
    val utilizationModel = new UtilizationModelFull
    //    val utilizationModel = new UtilizationModelDynamic(1)
    val list = (1 to CLOUDLETS).map(c => new CloudletSimple(CLOUDLET_LENGTH, CLOUDLET_PES, utilizationModel)).toList
    list.asJava
  }

  def main(args: Array[String]): Unit = {
    Simulation_part5
  }
}