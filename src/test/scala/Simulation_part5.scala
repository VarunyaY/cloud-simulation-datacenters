import simulations.Simulation_part5
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull
import org.cloudbus.cloudsim.vms.VmSimple
import org.scalatest.{FunSuite, stats}
import org.slf4j.{Logger, LoggerFactory}


class Simulation_part5 extends FunSuite {
  val SIM = "Simulation_2and3"
  val conf: Config = ConfigFactory.load(SIM+".conf")
  val LOG: Logger = LoggerFactory.getLogger(getClass)
  val numHosts = conf.getInt(SIM+".datacenter0.numberHosts")
  val numVMs = conf.getInt(SIM+".numVMs")
  val vm_PES: Int = conf.getInt(SIM + "." + "vm" + ".numberPES")
  val numCloudlets: Int = conf.getInt(SIM + "." + "numCloudlets")
  val cloudlet_PES: Int = conf.getInt(SIM + "." + "cloudlet" + ".numberPES")
  val cloudlet_length: Int = conf.getInt(SIM + "." + "cloudlet" + ".length")

  test("Simulation_part5.getOperatingSystemType") {
    val os = Simulation_part5.getOperatingSystemType(1)

    LOG.debug("Testing if operating system is being converted to a string by calling the method in the scala object")
    println(assert(os!=null))
  }

  test("Simulation_part5.getCloudletSchedulerType") {
    val st = Simulation_part5.getCloudletSchedulerType(1)

    LOG.debug("Testing if operating system is being converted to a string by calling the method in the scala object")
    println(assert(st!=null))
  }
}