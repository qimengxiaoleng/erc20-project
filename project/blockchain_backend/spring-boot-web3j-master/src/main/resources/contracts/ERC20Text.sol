// SPDX-License-Identifier: Apache-2.0
pragma solidity ^0.8.20;

// 引入OpenZeppelin提供的ERC20标准实现
import "./ERC20.sol";

contract ERC20Test is ERC20 {
    //初始化代币名称与符号
    constructor() ERC20("LMTToken", "LMT") {
    }

    // 铸造代币
    function mint(uint256 value) public {
        _mint(msg.sender, value);
    }

    // 销毁代币
    function burn(uint256 value) public {
        _burn(msg.sender, value);
    }
}