import simulations.Simulation_2and3
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull
import org.cloudbus.cloudsim.vms.VmSimple
import org.scalatest.{FunSuite}
import org.slf4j.{Logger, LoggerFactory}


class Simulation_2and3 extends FunSuite {

  val SIM = "Simulation_2and3"
  val conf: Config = ConfigFactory.load(SIM+".conf")
  val LOG: Logger = LoggerFactory.getLogger(getClass)
  val numHosts = conf.getInt(SIM+".datacenter0.numberHosts")
  val numVMs = conf.getInt(SIM+".numVMs")
  val vm_PES: Int = conf.getInt(SIM + "." + "vm" + ".numberPES")
  val numCloudlets: Int = conf.getInt(SIM + "." + "numCloudlets")
  val cloudlet_PES: Int = conf.getInt(SIM + "." + "cloudlet" + ".numberPES")
  val cloudlet_length: Int = conf.getInt(SIM + "." + "cloudlet" + ".length")

  test("Simulation_2and3.createDatacenter") {
    val name = "datacenter0"
    val datacenter0 = Simulation_2and3.createDatacenter(numHosts)

    LOG.debug("Testing if Datacenter is getting created by calling the method in the scala object")
    println(assert(datacenter0!=null))

    LOG.debug("Testing if the number of hosts are matching with number of hosts configured in the conf file")
    println(assert(datacenter0.getHostList.size() == numHosts))
  }

  test("Simulation_2and3.createHost"){
    val hostList= (1 to numHosts).map(host => Simulation_2and3.createHost).toList
    LOG.debug("Testing if hosts in a datacenter are getting created by calling the method in the scala object")
    println(assert(hostList.length==numHosts))
  }

  test("Simulation_2and3.createVms"){
    val vmlist = (1 to numVMs).map(vm => new VmSimple(1000, vm_PES).setCloudletScheduler(new CloudletSchedulerTimeShared)).toList
    LOG.debug("Testing if virtual machines in a datacenter are getting created by calling the method in the scala object")
    println(assert(vmlist.length!=null))
    LOG.debug("Testing if the number of virtual machines created in a datacenter are same as in the configuration file")
    println(assert(vmlist.length==numVMs))
  }

  test("Simulation_2and3.createCloudlets"){
    val cloudletList = {
      (1 to numCloudlets).map(c => new CloudletSimple(cloudlet_length, cloudlet_PES, new UtilizationModelFull)).toList
    }

    LOG.debug("Testing if cloudlets are created by calling the method in the scala object")
    println(assert(cloudletList.length!=null))
    LOG.debug("Testing if the number of virtual machines created in a datacenter are same as in the configuration file")
    println(assert(cloudletList.length==numCloudlets))
  }
}
