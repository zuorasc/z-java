package com.zuora.api.util;

/**
 * Created by earandes on 28/09/16.
 */
public class Configuration {

    private External external;

    public External getExternal() {
        return external;
    }

    public void setExternal(External external) {
        this.external = external;
    }

    public static class External {

        private Zuora zuora;

        public Zuora getZuora() {
            return zuora;
        }

        public void setZuora(Zuora zuora) {
            this.zuora = zuora;
        }

        public static class Zuora {

            private Soap soap;

            public Soap getSoap() {
                return soap;
            }

            public void setSoap(Soap soap) {
                this.soap = soap;
            }

            public static class Soap {
                private String username;
                private String password;
                private String endpoint;

                public String getUsername() {
                    return username;
                }

                public void setUsername(String username) {
                    this.username = username;
                }

                public String getPassword() {
                    return password;
                }

                public void setPassword(String password) {
                    this.password = password;
                }

                public String getEndpoint() {
                    return endpoint;
                }

                public void setEndpoint(String endpoint) {
                    this.endpoint = endpoint;
                }
            }
        }
    }
}
