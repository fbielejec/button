pragma solidity ^0.4.24;

import "./token/ERC721/ERC721Full.sol";

contract Button is ERC721Full("Button", "BUTT") {

  uint256 blockNumber = block.number;

  struct Token {
    uint256 blockNumber;
    uint256 weight;
    uint256 value;
    bytes imageHash;
  }

  event Press(uint256);

  // Array for mapping from tokenId to token data
  Token[] private _tokenData;

  event ImageHashSet(uint256 tokenId);

  function press() public payable {
    uint256 lastTokenBlockNumber = blockNumber;
    if (totalSupply() != 0) {
      lastTokenBlockNumber = _tokenData[totalSupply() - 1].blockNumber;
    }
    uint256 _tokenId;
    if (lastTokenBlockNumber == block.number) {
      _tokenId = totalSupply() - 1;
      Token storage _token = _tokenData[_tokenId];
      if (_token.value < msg.value) {
        address _from = ownerOf(_tokenId);
        _from.transfer(_token.value);
        _token.value = msg.value;
        _clearApproval(_from, _tokenId);
        _removeTokenFrom(_from, _tokenId);
        _addTokenTo(msg.sender, _tokenId);
        emit Transfer(_from, msg.sender, _tokenId);
      } else {
        msg.sender.transfer(msg.value);
      }
    } else {
      uint256 _weight = block.number - lastTokenBlockNumber;
      _tokenId = totalSupply();
      _mint(msg.sender, _tokenId);
      _tokenData.push(Token(block.number, _weight, msg.value, new bytes(0)));
    }
    emit Press(_tokenId);
  }

  function setImageHash(uint256 _tokenId, bytes _imageHash) public {
    require(msg.sender == ownerOf(_tokenId), "You are not the owner of this token");
    _tokenData[_tokenId].imageHash = _imageHash;
    emit ImageHashSet(_tokenId);
  }

  function loadToken(uint256 _tokenId) public view
    returns(uint256, uint256, uint256, bytes) {
    Token memory token = _tokenData[_tokenId];
    return (token.blockNumber,
            token.weight,
            token.value,
            token.imageHash);
  }

  function transferFrom(address _from, address _to, uint256 _tokenId) public {
    require(block.number != _tokenData[_tokenId].blockNumber);
    super.transferFrom(_from, _to, _tokenId);
  }

}
