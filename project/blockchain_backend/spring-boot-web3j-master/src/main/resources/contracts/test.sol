// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;
import "./ERC20.sol";


contract test {
    uint256 private storedData;

    event DataChanged(uint256 newValue);

    function set(uint256 x) public {
        storedData = x;
        emit DataChanged(x);
    }

    function get() public view returns (uint256) {
        return storedData;
    }
}