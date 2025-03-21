'use client'

import { Alert, Box, Button, Grid, Input, Sheet, Table } from "@mui/joy";
import { MuiFileInput } from "mui-file-input";
import React from "react";
import styles from "./page.module.css";

export default function Home() {
  const [privateKeyFile, setPrivateKeyFile] = React.useState<File | null>(null);
  const handlePrivateKeyFileChange = (newValue: File | null) => {
    setPrivateKeyFile(newValue);
  };
  const getJWTToken = async (privateKey: File) => {
    const formData = new FormData();
    formData.append("file", privateKey);

    const response = await fetch("/api/jwt", {
      method: "POST",
      body: formData
    });

    if (!response.ok) {
      throw new Error("Failed to get jwt token");
    }

    const respData = await response.json();
    return respData["token"];
  }
  const getLogs = async (path: string, nextPtr: number | null, token: string) => {
    const url = new URL(`http://localhost:3000/backend/logs/${path}`);
    url.searchParams.set("take", "10");
    if (contains.length > 0) url.searchParams.set("contains", contains);
    if (nextPtr) url.searchParams.set("ptr", nextPtr + "");

    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Authorization": token
      }
    });

    if (!response.ok) {
      throw new Error("Failed to get logs. Error: " + response.status);
    }

    const respData = await response.json();
    return respData;
  };

  const [path, setPath] = React.useState<string>("");
  const downloadLog = async (path: string, privateKey: File | null) => {
    try {
      if (privateKey === null) throw Error("Invalid private key");
      const token = await getJWTToken(privateKey);

      const resp = await getLogs(path, nextPtr, token);
      setLogs(resp["logs"]);
      setNextPtr(resp["nextPtr"]);
    } catch (e: unknown) {
      if (e instanceof Error)
        setAlert(e.message);
      else
        setAlert("unknown error occurred");
    }
  }

  const [logs, setLogs] = React.useState<string[]>([]);
  const [nextPtr, setNextPtr] = React.useState<number | null>(null);
  const [contains, setContains] = React.useState<string>("");
  const [alert, setAlert] = React.useState<string>("");

  return (
    <div className={styles.page}>
      {alert.length > 0 && (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, width: '100%' }}>
          <Alert
            variant="soft"
            color="danger"
            endDecorator={
              <Button size="sm" variant="solid" color="danger" onClick={() => setAlert("")}>
                Close
              </Button>
            }
          >
            {alert}
          </Alert>
        </Box>
      )}

      <Grid container>
        <Grid item>
          <MuiFileInput value={privateKeyFile} onChange={handlePrivateKeyFileChange} placeholder="select private key" />
        </Grid>
      </Grid>

      <Grid container spacing={2} sx={{}}>
        <Grid item size={6}>
          <Input
            onChange={(event) => setPath(event.target.value)}
            variant="soft"
            placeholder="log path"
            value={path} />
        </Grid>
        <Grid item size={6}>
          <Input
            onChange={(event) => setContains(event.target.value)}
            variant="soft"
            placeholder="filter by"
            value={contains} />
        </Grid>
        <Grid item size={6}>
          <Button disabled={(!privateKeyFile || (nextPtr && nextPtr < 0))} variant="solid" onClick={() => downloadLog(path, privateKeyFile)}>
            {!nextPtr ? "DOWNLOAD" : (nextPtr > 0 ? "NEXT >" : "NO MORE")}
          </Button>
        </Grid>
      </Grid>

      <Sheet>
        <Table>
          <thead>
            <tr>
              <th>log</th>
            </tr>
          </thead>
          {
            logs.map((l) => (
              <thead>
                <tr>
                  <th>{l}</th>
                </tr>
              </thead>
            ))
          }
        </Table>
      </Sheet>
    </div>
  );
}
