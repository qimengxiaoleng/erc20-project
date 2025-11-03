package com.wetech.demo.web3j.service;

import com.wetech.demo.web3j.contracts.erc20test.ERC20Test;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ERC20Service {

    private final Web3j web3j;
    private final Credentials credentials;
    private final ContractGasProvider gasProvider;

    private ERC20Test contract;
    @Getter
    private String contractAddress;

    /**
     * 检查合约是否已加载
     */
    public boolean isContractLoaded() {
        return contract != null;
    }

    /**
     * 获取合约状态信息
     */
    public String getContractStatus() {
        if (contract == null) {
            return "No contract loaded";
        }
        return "Contract loaded at: " + contractAddress;
    }

    /**
     * 部署ERC20合约
     */
    public CompletableFuture<String> deployContract() {
        log.info("Deploying ERC20Test contract...");
        return ERC20Test.deploy(web3j, credentials, gasProvider)
                .sendAsync()
                .thenApply(contract -> {
                    this.contract = contract;
                    this.contractAddress = contract.getContractAddress();
                    log.info("ERC20Test contract deployed to: {}", contractAddress);
                    return contractAddress;
                })
                .exceptionally(throwable -> {
                    log.error("Failed to deploy contract: {}", throwable.getMessage());
                    throw new RuntimeException("Contract deployment failed: " + throwable.getMessage());
                });
    }

    /**
     * 加载已部署的合约
     */
    public void loadContract(String contractAddress) {
        try {
            log.info("Loading ERC20Test contract from address: {}", contractAddress);
            this.contract = ERC20Test.load(contractAddress, web3j, credentials, gasProvider);
            this.contractAddress = contractAddress;
            log.info("Contract loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load contract: {}", e.getMessage());
            throw new RuntimeException("Contract loading failed: " + e.getMessage());
        }
    }

    /**
     * 铸造代币（mint）- 根据您的合约调整参数
     * @param value 代币数量
     */
    public CompletableFuture<TransactionReceipt> mint(BigInteger value) {
        if (contract == null) throw new IllegalStateException("Contract not deployed or loaded");
        log.info("Minting {} tokens", value);

        // 根据您的合约ABI调整参数
        return contract.mint(value).sendAsync()
                .thenApply(receipt -> {
                    if (!receipt.isStatusOK()) {
                        throw new RuntimeException("Mint transaction failed with status: " + receipt.getStatus());
                    }
                    log.info("Mint successful, transaction hash: {}", receipt.getTransactionHash());
                    return receipt;
                })
                .exceptionally(throwable -> {
                    log.error("Mint operation failed: {}", throwable.getMessage());
                    throw new RuntimeException("Mint failed: " + throwable.getMessage());
                });
    }

    /**
     * 转移代币（transfer）
     * @param to 接收地址
     * @param value 代币数量
     */
    public CompletableFuture<TransactionReceipt> transfer(String to, BigInteger value) {
        if (contract == null) throw new IllegalStateException("Contract not deployed or loaded");
        log.info("Transferring {} tokens to address: {}", value, to);

        return contract.transfer(to, value).sendAsync()
                .thenApply(receipt -> {
                    if (!receipt.isStatusOK()) {
                        throw new RuntimeException("Transfer transaction failed with status: " + receipt.getStatus());
                    }
                    log.info("Transfer successful, transaction hash: {}", receipt.getTransactionHash());
                    return receipt;
                })
                .exceptionally(throwable -> {
                    log.error("Transfer operation failed: {}", throwable.getMessage());
                    throw new RuntimeException("Transfer failed: " + throwable.getMessage());
                });
    }

    /**
     * 查询余额（balanceOf）
     * @param account 查询地址
     */
    public CompletableFuture<BigInteger> balanceOf(String account) {
        if (contract == null) throw new IllegalStateException("Contract not deployed or loaded");
        log.info("Getting balance for address: {}", account);

        return contract.balanceOf(account).sendAsync()
                .thenApply(balance -> {
                    log.info("Balance for {}: {}", account, balance);
                    return balance;
                })
                .exceptionally(throwable -> {
                    log.error("Balance query failed: {}", throwable.getMessage());
                    throw new RuntimeException("Balance query failed: " + throwable.getMessage());
                });
    }

    /**
     * 批准代币（approve）
     * @param spender 被授权地址
     * @param value 代币数量
     */
    public CompletableFuture<TransactionReceipt> approve(String spender, BigInteger value) {
        if (contract == null) throw new IllegalStateException("Contract not deployed or loaded");
        log.info("Approving {} tokens for spender: {}", value, spender);

        return contract.approve(spender, value).sendAsync()
                .thenApply(receipt -> {
                    if (!receipt.isStatusOK()) {
                        throw new RuntimeException("Approve transaction failed with status: " + receipt.getStatus());
                    }
                    log.info("Approve successful, transaction hash: {}", receipt.getTransactionHash());
                    return receipt;
                })
                .exceptionally(throwable -> {
                    log.error("Approve operation failed: {}", throwable.getMessage());
                    throw new RuntimeException("Approve failed: " + throwable.getMessage());
                });
    }

    /**
     * 从批准账户转移代币（transferFrom）
     * @param from 来源地址
     * @param to 接收地址
     * @param value 代币数量
     */
    public CompletableFuture<TransactionReceipt> transferFrom(String from, String to, BigInteger value) {
        if (contract == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Contract not deployed or loaded"));
        }

        log.info("TransferFrom - From: {}, To: {}, Value: {}", from, to, value);

        // 使用异步方式检查授权额度和余额
        CompletableFuture<BigInteger> allowanceFuture = contract.allowance(from, credentials.getAddress()).sendAsync();
        CompletableFuture<BigInteger> balanceFuture = contract.balanceOf(from).sendAsync();

        return allowanceFuture.thenCombine(balanceFuture, (allowance, balance) -> {
                    log.info("Allowance: {}, Balance: {}", allowance, balance);

                    if (allowance.compareTo(value) < 0) {
                        throw new RuntimeException("Insufficient allowance: " + allowance + " < " + value);
                    }
                    if (balance.compareTo(value) < 0) {
                        throw new RuntimeException("Insufficient balance: " + balance + " < " + value);
                    }

                    return contract.transferFrom(from, to, value).sendAsync();
                })
                .thenCompose(future -> future)
                .thenApply(receipt -> {
                    if (!receipt.isStatusOK()) {
                        throw new RuntimeException("TransferFrom failed with status: " + receipt.getStatus());
                    }
                    log.info("TransferFrom successful, txHash: {}", receipt.getTransactionHash());
                    return receipt;
                })
                .exceptionally(throwable -> {
                    log.error("TransferFrom operation failed: {}", throwable.getMessage());
                    throw new RuntimeException("TransferFrom failed: " + throwable.getMessage());
                });
    }


    /**
     * 查询授权额度
     */
    public CompletableFuture<BigInteger> allowance(String owner, String spender) {
        if (contract == null) throw new IllegalStateException("Contract not deployed or loaded");
        log.info("Querying allowance - Owner: {}, Spender: {}", owner, spender);

        return contract.allowance(owner, spender).sendAsync()
                .thenApply(allowance -> {
                    log.info("Allowance for spender {} from owner {}: {}", spender, owner, allowance);
                    return allowance;
                })
                .exceptionally(throwable -> {
                    log.error("Allowance query failed: {}", throwable.getMessage());
                    throw new RuntimeException("Allowance query failed: " + throwable.getMessage());
                });
    }

    // 在ERC20Service类中添加方法
    public String getDeployerAddress() {
        return credentials.getAddress();
    }


    /**
     * 获取代币名称
     */
    public CompletableFuture<String> name() {
        if (contract == null) throw new IllegalStateException("Contract not deployed or loaded");
        return contract.name().sendAsync();
    }

    /**
     * 获取代币符号
     */
    public CompletableFuture<String> symbol() {
        if (contract == null) throw new IllegalStateException("Contract not deployed or loaded");
        return contract.symbol().sendAsync();
    }

    /**
     * 获取代币小数位数
     */
    public CompletableFuture<BigInteger> decimals() {
        if (contract == null) throw new IllegalStateException("Contract not deployed or loaded");
        return contract.decimals().sendAsync();
    }

    /**
     * 获取总供应量
     */
    public CompletableFuture<BigInteger> totalSupply() {
        if (contract == null) throw new IllegalStateException("Contract not deployed or loaded");
        return contract.totalSupply().sendAsync();
    }
}