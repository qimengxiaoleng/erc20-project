package com.wetech.demo.web3j.controller;

import com.wetech.demo.web3j.service.ERC20Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/erc20")
@RequiredArgsConstructor
public class ERC20Controller {

    private final ERC20Service erc20Service;



    /**
     * 检查合约状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getContractStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("status", erc20Service.isContractLoaded() ? "LOADED" : "NOT_LOADED");
        response.put("contractAddress", erc20Service.getContractAddress());
        response.put("message", erc20Service.getContractStatus());
        return ResponseEntity.ok(response);
    }

    /**
     * 部署合约
     */
    @PostMapping("/deploy")
    public CompletableFuture<ResponseEntity<Map<String, String>>> deployContract() {
        return erc20Service.deployContract()
                .thenApply(address -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("contractAddress", address);
                    response.put("message", "Contract deployed successfully");
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("error", "Deployment failed: " + throwable.getMessage());
                    return ResponseEntity.badRequest().body(response);
                });
    }

    /**
     * 加载合约
     */
    @PostMapping("/load")
    public ResponseEntity<Map<String, String>> loadContract(@RequestParam String address) {
        try {
            erc20Service.loadContract(address);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Contract loaded successfully");
            response.put("contractAddress", address);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Load failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 铸造代币 - 修改为只接收数量参数
     */
    @PostMapping("/mint")
    public CompletableFuture<ResponseEntity<Map<String, String>>> mint(
            @RequestParam BigInteger value) {
        return erc20Service.mint(value)
                .thenApply(receipt -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("transactionHash", receipt.getTransactionHash());
                    response.put("blockNumber", receipt.getBlockNumber().toString());
                    response.put("gasUsed", receipt.getGasUsed().toString());
                    response.put("status", receipt.getStatus());
                    response.put("message", "Mint completed successfully");
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("error", "Mint failed: " + throwable.getMessage());
                    return ResponseEntity.badRequest().body(response);
                });
    }

    /**
     * 转移代币
     */
    @PostMapping("/transfer")
    public CompletableFuture<ResponseEntity<Map<String, String>>> transfer(
            @RequestParam String to,
            @RequestParam BigInteger value) {
        return erc20Service.transfer(to, value)
                .thenApply(receipt -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("transactionHash", receipt.getTransactionHash());
                    response.put("blockNumber", receipt.getBlockNumber().toString());
                    response.put("gasUsed", receipt.getGasUsed().toString());
                    response.put("status", receipt.getStatus());
                    response.put("message", "Transfer completed successfully");
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("error", "Transfer failed: " + throwable.getMessage());
                    return ResponseEntity.badRequest().body(response);
                });
    }

    /**
     * 查询余额
     */
    @GetMapping("/balanceOf")
    public CompletableFuture<ResponseEntity<Map<String, String>>> balanceOf(@RequestParam String account) {
        return erc20Service.balanceOf(account)
                .thenApply(balance -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("account", account);
                    response.put("balance", balance.toString());
                    response.put("message", "Balance query successful");
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("error", "Balance query failed: " + throwable.getMessage());
                    return ResponseEntity.badRequest().body(response);
                });
    }

    /**
     * 批准代币
     */
    @PostMapping("/approve")
    public CompletableFuture<ResponseEntity<Map<String, String>>> approve(
            @RequestParam String spender,
            @RequestParam BigInteger value) {
        return erc20Service.approve(spender, value)
                .thenApply(receipt -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("transactionHash", receipt.getTransactionHash());
                    response.put("blockNumber", receipt.getBlockNumber().toString());
                    response.put("gasUsed", receipt.getGasUsed().toString());
                    response.put("status", receipt.getStatus());
                    response.put("message", "Approve completed successfully");
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("error", "Approve failed: " + throwable.getMessage());
                    return ResponseEntity.badRequest().body(response);
                });
    }

    /**
     * 从批准账户转移代币
     */
    @PostMapping("/transferFrom")
    public CompletableFuture<ResponseEntity<Map<String, String>>> transferFrom(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigInteger value) {
        return erc20Service.transferFrom(from, to, value)
                .thenApply(receipt -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("transactionHash", receipt.getTransactionHash());
                    response.put("blockNumber", receipt.getBlockNumber().toString());
                    response.put("gasUsed", receipt.getGasUsed().toString());
                    response.put("status", receipt.getStatus());
                    response.put("message", "TransferFrom completed successfully");
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    Map<String, String> response = new HashMap<>();

                    // 提供更友好的错误消息
                    String errorMsg = throwable.getMessage();
                    if (errorMsg.contains("Insufficient allowance")) {
                        response.put("error", "授权额度不足，请先调用 approve 接口授权");
                        response.put("solution", "调用: POST /api/erc20/approve?spender=部署者地址&value=授权金额");
                    } else if (errorMsg.contains("Insufficient balance")) {
                        response.put("error", "账户余额不足");
                        response.put("solution", "请检查 from 账户的余额");
                    } else if (errorMsg.contains("Contract not loaded")) {
                        response.put("error", "合约未加载");
                        response.put("solution", "请先调用: POST /api/erc20/load?address=合约地址");
                    } else {
                        response.put("error", errorMsg);
                    }

                    return ResponseEntity.badRequest().body(response);
                });
    }

    /**
     * 获取当前合约地址
     */
    @GetMapping("/address")
    public ResponseEntity<Map<String, String>> getContractAddress() {
        String address = erc20Service.getContractAddress();
        Map<String, String> response = new HashMap<>();
        if (address != null) {
            response.put("contractAddress", address);
            response.put("status", "LOADED");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "No contract loaded");
            response.put("status", "NOT_LOADED");
            return ResponseEntity.ok(response);
        }
    }


    /**
     * 查询授权额度
     */
    @GetMapping("/allowance")
    public CompletableFuture<ResponseEntity<Map<String, String>>> allowance(
            @RequestParam String owner,
            @RequestParam String spender) {
        return erc20Service.allowance(owner, spender)
                .thenApply(allowance -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("owner", owner);
                    response.put("spender", spender);
                    response.put("allowance", allowance.toString());
                    response.put("message", "Allowance query successful");
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("error", "Allowance query failed: " + throwable.getMessage());
                    return ResponseEntity.badRequest().body(response);
                });
    }

    /**
     * 获取代币信息
     */
    @GetMapping("/info")
    public CompletableFuture<ResponseEntity<Map<String, String>>> getTokenInfo() {
        if (!erc20Service.isContractLoaded()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Contract not loaded");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }

        CompletableFuture<String> nameFuture = erc20Service.name();
        CompletableFuture<String> symbolFuture = erc20Service.symbol();
        CompletableFuture<BigInteger> decimalsFuture = erc20Service.decimals();
        CompletableFuture<BigInteger> totalSupplyFuture = erc20Service.totalSupply();

        return CompletableFuture.allOf(nameFuture, symbolFuture, decimalsFuture, totalSupplyFuture)
                .thenApply(v -> {
                    Map<String, String> response = new HashMap<>();
                    try {
                        response.put("name", nameFuture.get());
                        response.put("symbol", symbolFuture.get());
                        response.put("decimals", decimalsFuture.get().toString());
                        response.put("totalSupply", totalSupplyFuture.get().toString());
                        response.put("contractAddress", erc20Service.getContractAddress());
                        return ResponseEntity.ok(response);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to get token info: " + e.getMessage());
                    }
                })
                .exceptionally(throwable -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("error", "Token info query failed: " + throwable.getMessage());
                    return ResponseEntity.badRequest().body(response);
                });
    }
}