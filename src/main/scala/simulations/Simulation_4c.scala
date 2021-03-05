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

/*

Simulation 4 - RoundRobin VM Allocation Policy, Cloudlet TimeShared policy  and Cloudlet Dynamic Utilization model of 80%

 */

import java.util

import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyRandom, VmAllocationPolicyRoundRobin, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.slf4j.{Logger, LoggerFactory}

import scala.jdk.CollectionConverters._
import scala.jdk.javaapi.CollectionConverters.asJava

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
object Simulation_4c {
//Getting the configuraton values from the config file "Simulation_2and3.conf"
  val SIM = "Simulation_4c";
  val conf: Config = ConfigFactory.load(SIM + ".conf")
  val HOSTS: Int = conf.getInt(SIM + "." + "datacenter0" + ".numberHosts")
  val HOST_PES: Int = conf.getInt(SIM + "." + "host" + ".pes")
  val VMS: Int = conf.getInt(SIM + "." + "numVMs")
  val VM_PES: Int = conf.getInt(SIM + "." + "vm" + ".numberPES")
  val CLOUDLETS: Int = conf.getInt(SIM + "." + "numCloudlets")
  val CLOUDLET_PES: Int = conf.getInt(SIM + "." + "cloudlet" + ".numberPES")
  val CLOUDLET_LENGTH: Int = conf.getInt(SIM + "." + "cloudlet" + ".length")
//End of fetching the configuration values.

//  Instantiating the objects for the simulation
  val LOG: Logger = LoggerFactory.getLogger(getClass)
  val simulation = new CloudSim
//  Broker object to do the cloudlet mapping to a vm and to manage the cloudlets scheduling to the datacenter.
  val simplebroker = new DatacenterBrokerSimple(simulation)
//  Creates a datacenter object with the required number of hosts
  val datacenter0: DatacenterSimple = createDatacenter(HOSTS)
//  Creates a list of VMs for the cloudlets.
  val vmList: util.List[Vm] = createVms
//  Creates a list of Cloudlets which are the tasks from the customer.
  val cloudletList: util.List[CloudletSimple] = createCloudlets

  simplebroker.submitVmList(vmList)
  simplebroker.submitCloudletList(cloudletList)
  simulation.start
  val finishedCloudlets: util.List[Cloudlet] = simplebroker.getCloudletFinishedList
  new CloudletsTableBuilder(finishedCloudlets).build()
  println("---------------")
  println(CLOUDLETS*finishedCloudlets.get(0).getTotalCost)
  LOG.info("Simulation completed")


  /*
   To create a datacenter for the simulation.
   */
  def createDatacenter(numberHosts: Int): DatacenterSimple = {
    val hostList_new = (1 to numberHosts).map(host => createHost).toList
    val dc = new DatacenterSimple(simulation, hostList_new.asJava, new VmAllocationPolicyRoundRobin)
    dc.getCharacteristics
      .setCostPerSecond(conf.getInt(SIM + "." + "datacenter0" + ".cost"))
      .setCostPerBw(conf.getInt(SIM + "." + "datacenter0" + ".costPerBw"))
      .setCostPerMem(conf.getInt(SIM + "." + "datacenter0" +  ".costPerMem"))
      .setCostPerStorage(conf.getInt(SIM + "." + "datacenter0" +  ".costPerStorage"))
    dc
  }

  /*
   Method to create a list of Hosts in a datacenter.
   */
  def createHost: Host = {
    val peList = (1 to HOST_PES).map(pe => new PeSimple(1000)).toList
    val ram = conf.getInt(SIM + "." + "host" + ".ram") //in Megabytes
    val bw = conf.getInt(SIM + "." + "host" + ".bw") //in Megabits/s
    val storage = conf.getInt(SIM + "." + "host" + ".storage")
    new HostSimple(ram, bw, storage, asJava[Pe](peList))
  }

  /*
   Method to create a list of VMs with TimeShared scheduler.
   */
  def createVms: util.List[Vm] = {
    val list = (1 to VMS).map(vm => new VmSimple(1000, VM_PES)
                                        .setCloudletScheduler(new CloudletSchedulerTimeShared)).toList
    list.asJava
  }

  /*
   Method to create a list of Cloudlets.
   */
  def createCloudlets: util.List[CloudletSimple] = {
    /* UtilizationModel defining the cloudlets to use what percentage of the VM or any resource during the time
    of its execution. Considered dynamic utilization model for this simulation.*/
    val utilizationModel = new UtilizationModelDynamic(0.8)
    val list = (1 to CLOUDLETS).map(c => new CloudletSimple(CLOUDLET_LENGTH, CLOUDLET_PES, utilizationModel)).toList
    list.asJava
  }

  def main(args: Array[String]): Unit = {
    Simulation_2and3
  }
}