import jwt from 'jsonwebtoken';
import { NextResponse } from 'next/server';

export const POST = async (req: Request) => {
  const form = await req.formData();
  const file = form.get("file") as File | null;

  if (!file) {
    return NextResponse.json({}, {status: 400});
  }

  const buffer = Buffer.from(await file.arrayBuffer());
  const token = jwt.sign({userId: "haonguyen"}, buffer, { algorithm: "RS256"});
  return NextResponse.json({ token }, { status: 200 });
}