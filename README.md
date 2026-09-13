Architecture :

                       MAIN
                        │
            ┌───────────┴───────────┐
            │                       │
            ▼                       ▼
     AuthService                 Vault
            │                       │
            ▼                       ▼
          User                FileManager
                                    │
                         ┌──────────┼──────────┐
                         ▼          ▼          ▼
                       Add        List       Delete
