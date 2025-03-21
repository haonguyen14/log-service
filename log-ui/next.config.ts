import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  transpilePackages: ["mui-file-input"],
  async rewrites() {
    return [{
      source: "/backend/:path*",
      destination: "http://localhost:8080/:path*"
    }];
  }
};

export default nextConfig;
