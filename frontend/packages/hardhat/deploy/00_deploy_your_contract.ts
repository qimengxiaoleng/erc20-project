import { HardhatRuntimeEnvironment } from "hardhat/types";
import { DeployFunction } from "hardhat-deploy/types";

const func: DeployFunction = async function (hre: HardhatRuntimeEnvironment) {
  const { deployer } = await hre.getNamedAccounts();
  const { deploy } = hre.deployments;

  // 部署 ERC20LMT202330551171 合约
  const erc20 = await deploy("ERC20LMT202330551171", {
    from: deployer,
    args: [],
    log: true,
    autoMine: true,
  });

  console.log("🎉 ERC20LMT202330551171 合约部署成功！");
  console.log("📝 合约地址:", erc20.address);
  console.log("🔗 交易哈希:", erc20.transactionHash);
  console.log("👤 部署者:", deployer);
};

export default func;
func.tags = ["ERC20LMT202330551171"];