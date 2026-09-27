(ns musicaltec-app.ports.db.financial-movement
  (:require [musicaltec-app.domain.models.financial-movement :as models.financial-movement]
            [schema.core :as s]))

(s/defprotocol FinancialMovementRepository
  (insert! :- models.financial-movement/FinancialMovement
    [this
     movement :- models.financial-movement/FinancialMovement])
  (get! :- models.financial-movement/FinancialMovement
    [this
     id :- s/Uuid])
  (find-all :- [models.financial-movement/FinancialMovement]
    [this]))
