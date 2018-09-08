(ns button.shared.graphql-schema)

(def graphql-schema "
  scalar Date
  scalar Keyword

  type Query {

    allTokens(): [ButtonToken]
    lastPressBlockNumber(): Float
  }

  type ButtonToken {
    buttonToken_tokenId: ID
    buttonToken_number: Int
    buttonToken_ownerAddress: ID
    buttonToken_weight: Float
    buttonToken_imageHash: ID
  }

  ")
